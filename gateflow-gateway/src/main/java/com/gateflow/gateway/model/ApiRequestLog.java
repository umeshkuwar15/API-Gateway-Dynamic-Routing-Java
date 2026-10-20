package com.gateflow.gateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("api_request_logs")
public class ApiRequestLog {

    @Id
    private Long id;

    @Column("correlation_id")
    private String correlationId;

    @Column("request_timestamp")
    private LocalDateTime requestTimestamp;

    @Column("http_method")
    private String httpMethod;

    @Column("request_path")
    private String requestPath;

    @Column("matched_route_id")
    private String matchedRouteId;

    @Column("client_ip")
    private String clientIp;

    @Column("response_status")
    private Integer responseStatus;

    @Column("duration_ms")
    private Long durationMs;

    @Column("target_uri")
    private String targetUri;

    @Column("user_agent")
    private String userAgent;

    public ApiRequestLog() {
    }

    public ApiRequestLog(String correlationId, String httpMethod, String requestPath, String matchedRouteId, String clientIp, Integer responseStatus, Long durationMs, String targetUri, String userAgent) {
        this.correlationId = correlationId;
        this.requestTimestamp = LocalDateTime.now();
        this.httpMethod = httpMethod;
        this.requestPath = requestPath;
        this.matchedRouteId = matchedRouteId;
        this.clientIp = clientIp;
        this.responseStatus = responseStatus;
        this.durationMs = durationMs;
        this.targetUri = targetUri;
        this.userAgent = userAgent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getMatchedRouteId() {
        return matchedRouteId;
    }

    public void setMatchedRouteId(String matchedRouteId) {
        this.matchedRouteId = matchedRouteId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
