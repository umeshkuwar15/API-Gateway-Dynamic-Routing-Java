package com.gateflow.gateway.routing;

import com.gateflow.gateway.model.GatewayRoute;
import com.gateflow.gateway.repository.GatewayRouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseRouteDefinitionRepository implements RouteDefinitionRepository {

    private static final Logger log = LoggerFactory.getLogger(DatabaseRouteDefinitionRepository.class);

    private final GatewayRouteRepository gatewayRouteRepository;

    public DatabaseRouteDefinitionRepository(GatewayRouteRepository gatewayRouteRepository) {
        this.gatewayRouteRepository = gatewayRouteRepository;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return gatewayRouteRepository.findAllByEnabledTrue()
                .map(this::toRouteDefinition)
                .doOnNext(rd -> log.info("Loaded dynamic route definition: ID='{}', Path='{}', URI='{}'",
                        rd.getId(), rd.getPredicates(), rd.getUri()))
                .onErrorResume(ex -> {
                    log.error("Failed to load dynamic routes from MySQL repository: {}", ex.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(rd -> {
            GatewayRoute entity = new GatewayRoute();
            entity.setId(rd.getId());
            entity.setRouteName(rd.getId());
            entity.setDestinationUri(rd.getUri().toString());
            entity.setOrderIndex(rd.getOrder());
            entity.setEnabled(true);
            return gatewayRouteRepository.save(entity);
        }).then();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(gatewayRouteRepository::deleteById);
    }

    public RouteDefinition toRouteDefinition(GatewayRoute entity) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(entity.getId());
        definition.setUri(URI.create(entity.getDestinationUri()));
        definition.setOrder(entity.getOrderIndex());

        List<PredicateDefinition> predicates = new ArrayList<>();
        
        if (entity.getPathPredicate() != null && !entity.getPathPredicate().isBlank()) {
            PredicateDefinition path = new PredicateDefinition("Path=" + entity.getPathPredicate().trim());
            predicates.add(path);
        }

        
        if (entity.getHttpMethods() != null && !entity.getHttpMethods().isBlank() && !entity.getHttpMethods().equalsIgnoreCase("ALL")) {
            PredicateDefinition method = new PredicateDefinition("Method=" + entity.getHttpMethods().trim());
            predicates.add(method);
        }

        definition.setPredicates(predicates);

        
        List<FilterDefinition> filters = new ArrayList<>();
        if (entity.getStripPrefixParts() != null && entity.getStripPrefixParts() > 0) {
            FilterDefinition strip = new FilterDefinition("StripPrefix=" + entity.getStripPrefixParts());
            filters.add(strip);
        }
        definition.setFilters(filters);

        return definition;
    }
}
