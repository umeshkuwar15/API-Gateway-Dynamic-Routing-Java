package com.gateflow.gateway.controller;

import com.gateflow.gateway.model.BackendService;
import com.gateflow.gateway.repository.BackendServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/services")
public class AdminServiceController {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceController.class);

    private final BackendServiceRepository serviceRepository;
    private final WebClient webClient;

    public AdminServiceController(BackendServiceRepository serviceRepository, WebClient.Builder webClientBuilder) {
        this.serviceRepository = serviceRepository;
        this.webClient = webClientBuilder.build();
    }

    @GetMapping
    public Flux<BackendService> getAllServices() {
        return serviceRepository.findAll();
    }

    @PostMapping
    public Mono<ResponseEntity<BackendService>> registerService(@RequestBody BackendService service) {
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        service.setHealthStatus("UNKNOWN");
        return serviceRepository.save(service)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<BackendService>> updateService(@PathVariable Long id, @RequestBody BackendService updated) {
        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id)))
                .flatMap(existing -> {
                    existing.setServiceName(updated.getServiceName());
                    existing.setBaseUrl(updated.getBaseUrl());
                    existing.setHealthEndpoint(updated.getHealthEndpoint());
                    existing.setServiceType(updated.getServiceType());
                    existing.setEnabled(updated.isEnabled());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return serviceRepository.save(existing).map(ResponseEntity::ok);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteService(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id)))
                .flatMap(existing -> serviceRepository.deleteById(id).thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }

    @PostMapping("/{id}/health-check")
    public Mono<ResponseEntity<BackendService>> triggerHealthCheck(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id)))
                .flatMap(service -> {
                    String targetUrl = service.getBaseUrl() + service.getHealthEndpoint();
                    long startTime = System.currentTimeMillis();

                    return webClient.get()
                            .uri(targetUrl)
                            .retrieve()
                            .toBodilessEntity()
                            .timeout(Duration.ofSeconds(3))
                            .map(response -> {
                                long latency = System.currentTimeMillis() - startTime;
                                service.setHealthStatus(response.getStatusCode().is2xxSuccessful() ? "UP" : "DOWN");
                                service.setResponseTimeMs(latency);
                                service.setLastHealthCheckTime(LocalDateTime.now());
                                return service;
                            })
                            .onErrorResume(err -> {
                                long latency = System.currentTimeMillis() - startTime;
                                log.warn("Health check failed for service '{}' at {}: {}", service.getServiceName(), targetUrl, err.getMessage());
                                service.setHealthStatus("DOWN");
                                service.setResponseTimeMs(latency);
                                service.setLastHealthCheckTime(LocalDateTime.now());
                                return Mono.just(service);
                            })
                            .flatMap(serviceRepository::save)
                            .map(ResponseEntity::ok);
                });
    }
}
