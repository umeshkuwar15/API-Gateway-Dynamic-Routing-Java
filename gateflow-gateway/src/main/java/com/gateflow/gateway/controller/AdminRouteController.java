package com.gateflow.gateway.controller;

import com.gateflow.gateway.model.GatewayRoute;
import com.gateflow.gateway.repository.GatewayRouteRepository;
import com.gateflow.gateway.routing.RouteRefreshService;
import com.gateflow.gateway.validation.RouteValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/routes")
public class AdminRouteController {

    private final GatewayRouteRepository routeRepository;
    private final RouteRefreshService routeRefreshService;
    private final RouteValidator routeValidator;

    public AdminRouteController(GatewayRouteRepository routeRepository,
                                RouteRefreshService routeRefreshService,
                                RouteValidator routeValidator) {
        this.routeRepository = routeRepository;
        this.routeRefreshService = routeRefreshService;
        this.routeValidator = routeValidator;
    }

    @GetMapping
    public Flux<GatewayRoute> getAllRoutes() {
        return routeRepository.findAllOrdered();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<GatewayRoute>> getRouteById(@PathVariable String id) {
        return routeRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<GatewayRoute>> createRoute(@RequestBody GatewayRoute route) {
        routeValidator.validate(route);
        return routeRepository.existsById(route.getId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Route ID already exists: " + route.getId()));
                    }
                    route.setNewRecord(true);
                    route.setCreatedAt(LocalDateTime.now());
                    route.setUpdatedAt(LocalDateTime.now());
                    return routeRepository.save(route)
                            .flatMap(saved -> routeRefreshService.refreshRoutes().thenReturn(saved))
                            .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
                });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<GatewayRoute>> updateRoute(@PathVariable String id, @RequestBody GatewayRoute updated) {
        updated.setId(id);
        routeValidator.validate(updated);
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found with ID: " + id)))
                .flatMap(existing -> {
                    existing.setRouteName(updated.getRouteName());
                    existing.setPathPredicate(updated.getPathPredicate());
                    existing.setDestinationUri(updated.getDestinationUri());
                    existing.setHttpMethods(updated.getHttpMethods());
                    existing.setStripPrefixParts(updated.getStripPrefixParts());
                    existing.setOrderIndex(updated.getOrderIndex());
                    existing.setEnabled(updated.isEnabled());
                    existing.setDescription(updated.getDescription());
                    existing.setUpdatedAt(LocalDateTime.now());

                    return routeRepository.save(existing)
                            .flatMap(saved -> routeRefreshService.refreshRoutes().thenReturn(saved))
                            .map(ResponseEntity::ok);
                });
    }

    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<GatewayRoute>> toggleRouteStatus(@PathVariable String id, @RequestParam boolean enabled) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found with ID: " + id)))
                .flatMap(existing -> {
                    existing.setEnabled(enabled);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return routeRepository.save(existing)
                            .flatMap(saved -> routeRefreshService.refreshRoutes().thenReturn(saved))
                            .map(ResponseEntity::ok);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteRoute(@PathVariable String id) {
        return routeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found with ID: " + id)))
                .flatMap(existing -> routeRepository.deleteById(id)
                        .then(routeRefreshService.refreshRoutes())
                        .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }
}
