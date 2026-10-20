package com.gateflow.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class ProductHealthController {

    @GetMapping
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "gateflow-product-service");
        health.put("port", 8082);
        health.put("framework", "Spring Boot MVC & JPA");
        health.put("timestamp", Instant.now().toString());
        return health;
    }
}
