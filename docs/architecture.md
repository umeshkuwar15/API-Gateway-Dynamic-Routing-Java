# GateFlow: Architecture & Design Specification

## Overview
**GateFlow** is an enterprise-grade API Gateway with genuine runtime Dynamic Routing designed for microservice architectures.

```mermaid
graph TD
    User([Client / Browser]) -->|HTTP Port 8084| Dashboard[gateflow-dashboard<br/>Thymeleaf + Spring MVC]
    APIClient([External API Clients]) -->|HTTP Port 8080| Gateway[gateflow-gateway<br/>Spring Cloud Gateway - Reactive Netty]

    subgraph Internal Network
        Dashboard -.->|Secured Admin REST API| Gateway
        Gateway -->|Dynamic Route: /users/**| UserService[gateflow-user-service<br/>Port 8081]
        Gateway -->|Dynamic Route: /products/**| ProductService[gateflow-product-service<br/>Port 8082]
        Gateway -->|Dynamic Route: /orders/**| OrderService[gateflow-order-service<br/>Port 8083]

        Gateway -.->|R2DBC Reactive| MySQL[(MySQL Database<br/>gateflow_db:3306)]
        Dashboard -.->|JDBC / Flyway| MySQL
        UserService -.->|JPA / JDBC| MySQL
        ProductService -.->|JPA / JDBC| MySQL
        OrderService -.->|JPA / JDBC| MySQL
    end
```

## Architectural Decoupling
1. **GateFlow Gateway (`gateflow-gateway`, Port 8080)**:
   - Built on Netty, Spring WebFlux, and Spring Cloud Gateway.
   - Non-blocking I/O throughout the request pipeline.
   - Reads route definitions dynamically from MySQL via Spring Data R2DBC.
   - Refreshes route definitions in memory via Spring Cloud's `RefreshRoutesEvent`.
   - No blocking Spring MVC or blocking JPA dependencies.

2. **GateFlow Dashboard (`gateflow-dashboard`, Port 8084)**:
   - Built on Spring MVC and Thymeleaf with Bootstrap 5.
   - Manages routes, services, logs, and testing via secure HTTP administration APIs exposed by the Gateway.
   - Session & CSRF protected.

3. **Business Microservices**:
   - User Service (`gateflow-user-service`, Port 8081)
   - Product Service (`gateflow-product-service`, Port 8082)
   - Order Service (`gateflow-order-service`, Port 8083)
