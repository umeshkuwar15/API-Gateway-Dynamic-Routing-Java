package com.gateflow.gateway.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RouteRefreshService {

    private static final Logger log = LoggerFactory.getLogger(RouteRefreshService.class);

    private final ApplicationEventPublisher publisher;

    public RouteRefreshService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public Mono<Void> refreshRoutes() {
        return Mono.fromRunnable(() -> {
            log.info("Publishing RefreshRoutesEvent to dynamically synchronize Spring Cloud Gateway routing table...");
            publisher.publishEvent(new RefreshRoutesEvent(this));
            log.info("RefreshRoutesEvent successfully broadcasted.");
        });
    }
}
