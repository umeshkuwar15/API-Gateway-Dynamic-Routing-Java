package com.gateflow.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class GatewayClientService {

    private static final Logger log = LoggerFactory.getLogger(GatewayClientService.class);

    private final RestClient restClient;
    private final String gatewayBaseUrl;
    private final String adminSecret;

    public GatewayClientService(
            @Value("${gateflow.gateway.url:http://localhost:8080}") String gatewayBaseUrl,
            @Value("${gateflow.gateway.admin-secret:GateFlowSuperSecretKey2026AdminTokenForInternalApiAuth}") String adminSecret) {
        this.gatewayBaseUrl = gatewayBaseUrl;
        this.adminSecret = adminSecret;
        this.restClient = RestClient.builder()
                .baseUrl(gatewayBaseUrl)
                .defaultHeader("X-GateFlow-Admin-Secret", adminSecret)
                .build();
    }

    public String getGatewayBaseUrl() {
        return gatewayBaseUrl;
    }

    public Map<String, Object> getDashboardStats() {
        try {
            return restClient.get()
                    .uri("/api/admin/stats")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to fetch dashboard stats from gateway: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("totalRoutes", 0);
            fallback.put("enabledRoutes", 0);
            fallback.put("disabledRoutes", 0);
            fallback.put("registeredServices", 0);
            fallback.put("totalRequests", 0);
            fallback.put("successfulRequests", 0);
            fallback.put("failedRequests", 0);
            fallback.put("recentLogs", Collections.emptyList());
            return fallback;
        }
    }

    public List<Map<String, Object>> getAllRoutes() {
        try {
            return restClient.get()
                    .uri("/api/admin/routes")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch routes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getRouteById(String id) {
        try {
            return restClient.get()
                    .uri("/api/admin/routes/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch route {}: {}", id, e.getMessage());
            return null;
        }
    }

    public void createRoute(Map<String, Object> routeData) {
        restClient.post()
                .uri("/api/admin/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(routeData)
                .retrieve()
                .toBodilessEntity();
    }

    public void updateRoute(String id, Map<String, Object> routeData) {
        restClient.put()
                .uri("/api/admin/routes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(routeData)
                .retrieve()
                .toBodilessEntity();
    }

    public void toggleRouteStatus(String id, boolean enabled) {
        restClient.patch()
                .uri("/api/admin/routes/{id}/status?enabled={enabled}", id, enabled)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteRoute(String id) {
        restClient.delete()
                .uri("/api/admin/routes/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public List<Map<String, Object>> getAllServices() {
        try {
            return restClient.get()
                    .uri("/api/admin/services")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch services: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void registerService(Map<String, Object> serviceData) {
        restClient.post()
                .uri("/api/admin/services")
                .contentType(MediaType.APPLICATION_JSON)
                .body(serviceData)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteService(Long id) {
        restClient.delete()
                .uri("/api/admin/services/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public Map<String, Object> triggerServiceHealthCheck(Long id) {
        try {
            return restClient.post()
                    .uri("/api/admin/services/{id}/health-check", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Health check call failed for service {}: {}", id, e.getMessage());
            return Collections.emptyMap();
        }
    }

    public List<Map<String, Object>> getRecentLogs() {
        try {
            return restClient.get()
                    .uri("/api/admin/logs")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch logs: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Object> authenticateAdmin(String username, String password) {
        try {
            Map<String, String> credentials = Map.of("username", username, "password", password);
            return restClient.post()
                    .uri("/api/admin/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(credentials)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Admin login authentication rejected: {}", e.getMessage());
            return null;
        }
    }

    public Map<String, Object> executeApiTest(String method, String path, String body) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        String fullUrl = gatewayBaseUrl + normalizedPath;
        result.put("url", fullUrl);
        result.put("method", method.toUpperCase());

        try {
            RestClient testClient = RestClient.create();
            RestClient.RequestBodySpec requestSpec = testClient
                    .method(HttpMethod.valueOf(method.toUpperCase()))
                    .uri(fullUrl)
                    .header("Accept", "application/json");

            if (body != null && !body.isBlank() && !method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("DELETE")) {
                requestSpec.contentType(MediaType.APPLICATION_JSON).body(body);
            }

            ResponseEntity<String> response = requestSpec.retrieve().toEntity(String.class);
            long latency = System.currentTimeMillis() - startTime;

            result.put("status", response.getStatusCode().value());
            result.put("statusText", response.getStatusCode().toString());
            result.put("responseTimeMs", latency);
            result.put("headers", response.getHeaders().toSingleValueMap());
            result.put("responseBody", response.getBody() != null ? response.getBody() : "{}");
            result.put("success", response.getStatusCode().is2xxSuccessful());
        } catch (HttpStatusCodeException ex) {
            long latency = System.currentTimeMillis() - startTime;
            result.put("status", ex.getStatusCode().value());
            result.put("statusText", ex.getStatusCode().toString());
            result.put("responseTimeMs", latency);
            result.put("headers", ex.getResponseHeaders() != null ? ex.getResponseHeaders().toSingleValueMap() : Collections.emptyMap());
            result.put("responseBody", ex.getResponseBodyAsString());
            result.put("success", false);
        } catch (ResourceAccessException ex) {
            long latency = System.currentTimeMillis() - startTime;
            result.put("status", 503);
            result.put("statusText", "Service Unavailable (Gateway or Backend unreachable)");
            result.put("responseTimeMs", latency);
            result.put("headers", Collections.emptyMap());
            result.put("responseBody", "{\"error\": \"" + ex.getMessage() + "\"}");
            result.put("success", false);
        } catch (Exception ex) {
            long latency = System.currentTimeMillis() - startTime;
            result.put("status", 500);
            result.put("statusText", "Internal Error: " + ex.getMessage());
            result.put("responseTimeMs", latency);
            result.put("headers", Collections.emptyMap());
            result.put("responseBody", "{\"error\": \"" + ex.getMessage() + "\"}");
            result.put("success", false);
        }

        return result;
    }
}
