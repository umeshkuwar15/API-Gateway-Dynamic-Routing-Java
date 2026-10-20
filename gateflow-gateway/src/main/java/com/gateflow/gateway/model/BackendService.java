package com.gateflow.gateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("backend_services")
public class BackendService {

    @Id
    private Long id;

    @Column("service_id")
    private String serviceId;

    @Column("service_name")
    private String serviceName;

    @Column("base_url")
    private String baseUrl;

    @Column("health_endpoint")
    private String healthEndpoint = "/api/health";

    @Column("service_type")
    private String serviceType = "REST";

    private boolean enabled = true;

    @Column("health_status")
    private String healthStatus = "UNKNOWN";

    @Column("last_health_check_time")
    private LocalDateTime lastHealthCheckTime;

    @Column("response_time_ms")
    private Long responseTimeMs = 0L;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public BackendService() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getHealthEndpoint() {
        return healthEndpoint != null ? healthEndpoint : "/api/health";
    }

    public void setHealthEndpoint(String healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHealthStatus() {
        return healthStatus != null ? healthStatus : "UNKNOWN";
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public LocalDateTime getLastHealthCheckTime() {
        return lastHealthCheckTime;
    }

    public void setLastHealthCheckTime(LocalDateTime lastHealthCheckTime) {
        this.lastHealthCheckTime = lastHealthCheckTime;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs != null ? responseTimeMs : 0L;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
