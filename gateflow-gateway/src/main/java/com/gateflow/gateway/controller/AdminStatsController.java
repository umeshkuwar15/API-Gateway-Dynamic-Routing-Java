package com.gateflow.gateway.controller;

import com.gateflow.gateway.model.ApiRequestLog;
import com.gateflow.gateway.repository.ApiRequestLogRepository;
import com.gateflow.gateway.repository.BackendServiceRepository;
import com.gateflow.gateway.repository.GatewayRouteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final GatewayRouteRepository routeRepository;
    private final BackendServiceRepository serviceRepository;
    private final ApiRequestLogRepository logRepository;

    public AdminStatsController(GatewayRouteRepository routeRepository,
                                BackendServiceRepository serviceRepository,
                                ApiRequestLogRepository logRepository) {
        this.routeRepository = routeRepository;
        this.serviceRepository = serviceRepository;
        this.logRepository = logRepository;
    }

    @GetMapping
    public Mono<Map<String, Object>> getDashboardStatistics() {
        return Mono.zip(
                routeRepository.count(),
                routeRepository.findAllByEnabledTrue().count(),
                serviceRepository.count(),
                logRepository.count(),
                logRepository.countSuccessfulRequests().defaultIfEmpty(0L),
                logRepository.countFailedRequests().defaultIfEmpty(0L),
                logRepository.findRecentLogs().take(10).collectList().defaultIfEmpty(List.of())
        ).map(tuple -> {
            long totalRoutes = tuple.getT1();
            long enabledRoutes = tuple.getT2();
            long disabledRoutes = Math.max(0, totalRoutes - enabledRoutes);
            long registeredServices = tuple.getT3();
            long totalRequests = tuple.getT4();
            long successfulRequests = tuple.getT5();
            long failedRequests = tuple.getT6();
            List<ApiRequestLog> recentLogs = tuple.getT7();

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalRoutes", totalRoutes);
            stats.put("enabledRoutes", enabledRoutes);
            stats.put("disabledRoutes", disabledRoutes);
            stats.put("registeredServices", registeredServices);
            stats.put("totalRequests", totalRequests);
            stats.put("successfulRequests", successfulRequests);
            stats.put("failedRequests", failedRequests);
            stats.put("recentLogs", recentLogs);
            return stats;
        });
    }
}
