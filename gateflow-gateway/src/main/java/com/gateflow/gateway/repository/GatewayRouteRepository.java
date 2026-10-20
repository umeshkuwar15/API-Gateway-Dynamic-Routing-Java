package com.gateflow.gateway.repository;

import com.gateflow.gateway.model.GatewayRoute;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface GatewayRouteRepository extends ReactiveCrudRepository<GatewayRoute, String> {

    Flux<GatewayRoute> findAllByEnabledTrue();

    @Query("SELECT * FROM gateway_routes ORDER BY order_index ASC, created_at ASC")
    Flux<GatewayRoute> findAllOrdered();

    Flux<GatewayRoute> findByEnabled(boolean enabled);
}
