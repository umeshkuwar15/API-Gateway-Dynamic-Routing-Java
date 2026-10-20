package com.gateflow.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class GatewayHealthController {

    @GetMapping
    public Mono<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "gateflow-gateway");
        health.put("port", 8080);
        health.put("framework", "Spring Cloud Gateway (Reactive / WebFlux)");
        health.put("timestamp", Instant.now().toString());
        return Mono.just(health);
    }
}
