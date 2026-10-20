package com.gateflow.dashboard.controller;

import com.gateflow.dashboard.service.GatewayClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/logs")
public class RequestLogsController {

    private final GatewayClientService gatewayClientService;

    public RequestLogsController(GatewayClientService gatewayClientService) {
        this.gatewayClientService = gatewayClientService;
    }

    @GetMapping
    public String viewLogs(Model model) {
        List<Map<String, Object>> logs = gatewayClientService.getRecentLogs();
        model.addAttribute("logs", logs);
        return "logs";
    }
}
