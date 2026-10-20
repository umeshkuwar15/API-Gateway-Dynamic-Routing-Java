package com.gateflow.dashboard.controller;

import com.gateflow.dashboard.service.GatewayClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class DashboardController {

    private final GatewayClientService gatewayClientService;

    public DashboardController(GatewayClientService gatewayClientService) {
        this.gatewayClientService = gatewayClientService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboardHome(Model model) {
        Map<String, Object> stats = gatewayClientService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("gatewayUrl", gatewayClientService.getGatewayBaseUrl());
        return "dashboard";
    }
}
