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
@RequestMapping("/routes")
public class RouteManagementController {

    private final GatewayClientService gatewayClientService;

    public RouteManagementController(GatewayClientService gatewayClientService) {
        this.gatewayClientService = gatewayClientService;
    }

    @GetMapping
    public String listRoutes(Model model) {
        List<Map<String, Object>> routes = gatewayClientService.getAllRoutes();
        model.addAttribute("routes", routes);
        return "routes";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Map<String, Object> route = new HashMap<>();
        route.put("id", "");
        route.put("routeName", "");
        route.put("pathPredicate", "/");
        route.put("destinationUri", "http://localhost:");
        route.put("httpMethods", "GET,POST,PUT,DELETE,PATCH");
        route.put("stripPrefixParts", 0);
        route.put("orderIndex", 0);
        route.put("enabled", true);
        route.put("description", "");

        model.addAttribute("route", route);
        model.addAttribute("isEdit", false);
        return "route-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Map<String, Object> route = gatewayClientService.getRouteById(id);
        if (route == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Route not found: " + id);
            return "redirect:/routes";
        }
        model.addAttribute("route", route);
        model.addAttribute("isEdit", true);
        return "route-form";
    }

    @PostMapping("/save")
    public String saveRoute(@RequestParam(value = "isEdit", defaultValue = "false") boolean isEdit,
                            @RequestParam("id") String id,
                            @RequestParam("routeName") String routeName,
                            @RequestParam("pathPredicate") String pathPredicate,
                            @RequestParam("destinationUri") String destinationUri,
                            @RequestParam(value = "httpMethods", defaultValue = "GET,POST,PUT,DELETE,PATCH") String httpMethods,
                            @RequestParam(value = "stripPrefixParts", defaultValue = "0") int stripPrefixParts,
                            @RequestParam(value = "orderIndex", defaultValue = "0") int orderIndex,
                            @RequestParam(value = "enabled", defaultValue = "false") boolean enabled,
                            @RequestParam(value = "description", required = false) String description,
                            RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> routeData = new HashMap<>();
            routeData.put("id", id.trim());
            routeData.put("routeName", routeName.trim());
            routeData.put("pathPredicate", pathPredicate.trim());
            routeData.put("destinationUri", destinationUri.trim());
            routeData.put("httpMethods", httpMethods.trim());
            routeData.put("stripPrefixParts", stripPrefixParts);
            routeData.put("orderIndex", orderIndex);
            routeData.put("enabled", enabled);
            routeData.put("description", description != null ? description.trim() : "");

            if (isEdit) {
                gatewayClientService.updateRoute(id.trim(), routeData);
                redirectAttributes.addFlashAttribute("successMessage", "Route '" + id + "' successfully updated and refreshed in Gateway!");
            } else {
                gatewayClientService.createRoute(routeData);
                redirectAttributes.addFlashAttribute("successMessage", "New route '" + id + "' created and activated at runtime!");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save route: " + ex.getMessage());
        }
        return "redirect:/routes";
    }

    @PostMapping("/toggle/{id}")
    public String toggleRoute(@PathVariable String id, @RequestParam("enabled") boolean enabled, RedirectAttributes redirectAttributes) {
        try {
            gatewayClientService.toggleRouteStatus(id, enabled);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Route '" + id + "' has been " + (enabled ? "enabled" : "disabled") + " and dynamically synchronized!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update route status: " + ex.getMessage());
        }
        return "redirect:/routes";
    }

    @PostMapping("/delete/{id}")
    public String deleteRoute(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            gatewayClientService.deleteRoute(id);
            redirectAttributes.addFlashAttribute("successMessage", "Route '" + id + "' successfully deleted and removed from active gateway definitions.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete route: " + ex.getMessage());
        }
        return "redirect:/routes";
    }
}
