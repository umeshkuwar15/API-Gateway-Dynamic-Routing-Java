package com.gateflow.gateway.filter;

import com.gateflow.gateway.model.ApiRequestLog;
import com.gateflow.gateway.repository.ApiRequestLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.UUID;

@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingGlobalFilter.class);

    private final ApiRequestLogRepository apiRequestLogRepository;

    public RequestLoggingGlobalFilter(ApiRequestLogRepository apiRequestLogRepository) {
        this.apiRequestLogRepository = apiRequestLogRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        
        if (path.startsWith("/actuator") || path.startsWith("/api/health") || path.startsWith("/api/admin/logs")) {
            return chain.filter(exchange);
        }

        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().add("X-GateFlow-Correlation-ID", correlationId);

        String httpMethod = exchange.getRequest().getMethod().name();
        String clientIp = resolveClientIp(exchange);
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
            int status = statusCode != null ? statusCode.value() : 200;

            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeId = (route != null) ? route.getId() : "UNMATCHED";
            URI targetUri = (route != null) ? route.getUri() : null;

            ApiRequestLog auditLog = new ApiRequestLog(
                    correlationId,
                    httpMethod,
                    path,
                    routeId,
                    clientIp,
                    status,
                    duration,
                    targetUri != null ? targetUri.toString() : null,
                    userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent
            );

            apiRequestLogRepository.save(auditLog)
                    .doOnError(err -> log.warn("Failed to persist audit log to MySQL: {}", err.getMessage()))
                    .subscribe();

            log.info("GATEFLOW [{}] {} {} -> Route='{}', Status={}, Duration={}ms",
                    correlationId.substring(0, 8), httpMethod, path, routeId, status, duration);
        }));
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
