package com.gateflow.gateway.repository;

import com.gateflow.gateway.model.BackendService;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BackendServiceRepository extends ReactiveCrudRepository<BackendService, Long> {
    Mono<BackendService> findByServiceId(String serviceId);
    Flux<BackendService> findAllByEnabledTrue();
}
