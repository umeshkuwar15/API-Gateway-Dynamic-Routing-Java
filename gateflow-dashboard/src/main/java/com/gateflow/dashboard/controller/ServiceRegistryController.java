package com.gateflow.dashboard.controller;

import com.gateflow.dashboard.service.GatewayClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/services")
public class ServiceRegistryController {

    private final GatewayClientService gatewayClientService;

    public ServiceRegistryController(GatewayClientService gatewayClientService) {
        this.gatewayClientService = gatewayClientService;
    }

    @GetMapping
    public String listServices(Model model) {
        List<Map<String, Object>> services = gatewayClientService.getAllServices();
        model.addAttribute("services", services);
        return "services";
    }

    @PostMapping("/save")
    public String saveService(@RequestParam("serviceId") String serviceId,
                              @RequestParam("serviceName") String serviceName,
                              @RequestParam("baseUrl") String baseUrl,
                              @RequestParam(value = "healthEndpoint", defaultValue = "/api/health") String healthEndpoint,
                              @RequestParam(value = "serviceType", defaultValue = "REST") String serviceType,
                              @RequestParam(value = "enabled", defaultValue = "false") boolean enabled,
                              RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("serviceId", serviceId.trim());
            data.put("serviceName", serviceName.trim());
            data.put("baseUrl", baseUrl.trim());
            data.put("healthEndpoint", healthEndpoint.trim());
            data.put("serviceType", serviceType.trim());
            data.put("enabled", enabled);

            gatewayClientService.registerService(data);
            redirectAttributes.addFlashAttribute("successMessage", "Service '" + serviceName + "' successfully registered!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to register service: " + ex.getMessage());
        }
        return "redirect:/services";
    }

    @PostMapping("/check/{id}")
    public String checkHealth(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> updated = gatewayClientService.triggerServiceHealthCheck(id);
            String status = String.valueOf(updated.get("healthStatus"));
            redirectAttributes.addFlashAttribute("successMessage",
                    "Health check completed. Service status: " + status + " (" + updated.get("responseTimeMs") + " ms)");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Health check execution failed: " + ex.getMessage());
        }
        return "redirect:/services";
    }

    @PostMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            gatewayClientService.deleteService(id);
            redirectAttributes.addFlashAttribute("successMessage", "Service removed from registry.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete service: " + ex.getMessage());
        }
        return "redirect:/services";
    }
}
