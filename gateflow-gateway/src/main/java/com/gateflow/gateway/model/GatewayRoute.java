package com.gateflow.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("gateway_routes")
public class GatewayRoute implements Persistable<String> {

    @Id
    private String id;

    @Transient
    @JsonIgnore
    private boolean isNewRecord = false;

    @Column("route_name")
    private String routeName;

    @Column("path_predicate")
    private String pathPredicate;

    @Column("destination_uri")
    private String destinationUri;

    @Column("http_methods")
    private String httpMethods = "GET,POST,PUT,DELETE,PATCH";

    @Column("strip_prefix_parts")
    private Integer stripPrefixParts = 0;

    @Column("rate_limit_replenish_rate")
    private Integer rateLimitReplenishRate;

    @Column("rate_limit_burst_capacity")
    private Integer rateLimitBurstCapacity;

    @Column("order_index")
    private Integer orderIndex = 0;

    private boolean enabled = true;

    private String description;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public GatewayRoute() {
    }

    public GatewayRoute(String id, String routeName, String pathPredicate, String destinationUri, String httpMethods, boolean enabled, String description) {
        this.id = id;
        this.routeName = routeName;
        this.pathPredicate = pathPredicate;
        this.destinationUri = destinationUri;
        this.httpMethods = httpMethods;
        this.enabled = enabled;
        this.description = description;
    }

    @Override
    public boolean isNew() {
        return this.isNewRecord;
    }

    public void setNewRecord(boolean isNewRecord) {
        this.isNewRecord = isNewRecord;
    }

    
    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getPathPredicate() {
        return pathPredicate;
    }

    public void setPathPredicate(String pathPredicate) {
        this.pathPredicate = pathPredicate;
    }

    public String getDestinationUri() {
        return destinationUri;
    }

    public void setDestinationUri(String destinationUri) {
        this.destinationUri = destinationUri;
    }

    public String getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(String httpMethods) {
        this.httpMethods = httpMethods;
    }

    public Integer getStripPrefixParts() {
        return stripPrefixParts != null ? stripPrefixParts : 0;
    }

    public void setStripPrefixParts(Integer stripPrefixParts) {
        this.stripPrefixParts = stripPrefixParts;
    }

    public Integer getRateLimitReplenishRate() {
        return rateLimitReplenishRate;
    }

    public void setRateLimitReplenishRate(Integer rateLimitReplenishRate) {
        this.rateLimitReplenishRate = rateLimitReplenishRate;
    }

    public Integer getRateLimitBurstCapacity() {
        return rateLimitBurstCapacity;
    }

    public void setRateLimitBurstCapacity(Integer rateLimitBurstCapacity) {
        this.rateLimitBurstCapacity = rateLimitBurstCapacity;
    }

    public Integer getOrderIndex() {
        return orderIndex != null ? orderIndex : 0;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
