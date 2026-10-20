package com.gateflow.gateway.repository;

import com.gateflow.gateway.model.ApiRequestLog;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ApiRequestLogRepository extends ReactiveCrudRepository<ApiRequestLog, Long> {

    @Query("SELECT * FROM api_request_logs ORDER BY request_timestamp DESC LIMIT 100")
    Flux<ApiRequestLog> findRecentLogs();

    @Query("SELECT COUNT(*) FROM api_request_logs WHERE response_status < 400")
    Mono<Long> countSuccessfulRequests();

    @Query("SELECT COUNT(*) FROM api_request_logs WHERE response_status >= 400")
    Mono<Long> countFailedRequests();
}
