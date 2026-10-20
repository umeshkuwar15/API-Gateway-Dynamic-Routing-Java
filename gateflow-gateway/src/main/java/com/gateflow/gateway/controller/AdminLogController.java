package com.gateflow.gateway.controller;

import com.gateflow.gateway.model.ApiRequestLog;
import com.gateflow.gateway.repository.ApiRequestLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

    private final ApiRequestLogRepository logRepository;

    public AdminLogController(ApiRequestLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping
    public Flux<ApiRequestLog> getRecentLogs() {
        return logRepository.findRecentLogs();
    }
}
