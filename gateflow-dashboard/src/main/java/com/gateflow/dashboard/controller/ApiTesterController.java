package com.gateflow.dashboard.controller;

import com.gateflow.dashboard.service.GatewayClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/tester")
public class ApiTesterController {

    private final GatewayClientService gatewayClientService;

    public ApiTesterController(GatewayClientService gatewayClientService) {
        this.gatewayClientService = gatewayClientService;
    }

    @GetMapping
    public String testerPage(Model model) {
        model.addAttribute("gatewayUrl", gatewayClientService.getGatewayBaseUrl());
        model.addAttribute("method", "GET");
        model.addAttribute("path", "/users");
        model.addAttribute("body", "{\n  \"username\": \"test_user\",\n  \"email\": \"test@example.com\",\n  \"firstName\": \"Test\",\n  \"lastName\": \"User\"\n}");
        model.addAttribute("hasResponse", false);
        return "tester";
    }

    @PostMapping("/send")
    public String executeTest(@RequestParam("method") String method,
                             @RequestParam("path") String path,
                             @RequestParam(value = "body", required = false) String body,
                             Model model) {
        Map<String, Object> testResult = gatewayClientService.executeApiTest(method, path, body);

        model.addAttribute("gatewayUrl", gatewayClientService.getGatewayBaseUrl());
        model.addAttribute("method", method);
        model.addAttribute("path", path);
        model.addAttribute("body", body);
        model.addAttribute("hasResponse", true);
        model.addAttribute("result", testResult);

        return "tester";
    }
}
