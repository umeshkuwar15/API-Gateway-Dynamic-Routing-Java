package com.gateflow.gateway;

import com.gateflow.gateway.model.GatewayRoute;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GateFlowGatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
    }

    @Test
    void healthCheckReturnsUp() {
        webTestClient.get()
                .uri("/api/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("gateflow-gateway");
    }

    @Test
    void dynamicRouteCrudLifecycle() {
        
        GatewayRoute testRoute = new GatewayRoute();
        testRoute.setId("dynamic-test-route");
        testRoute.setRouteName("Dynamic Test Route");
        testRoute.setPathPredicate("/test-service/**");
        testRoute.setDestinationUri("http://localhost:8081");
        testRoute.setHttpMethods("GET,POST");
        testRoute.setEnabled(true);
        testRoute.setDescription("Test dynamic routing at runtime");

        webTestClient.post()
                .uri("/api/admin/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testRoute)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("dynamic-test-route")
                .jsonPath("$.routeName").isEqualTo("Dynamic Test Route");

        
        webTestClient.get()
                .uri("/api/admin/routes/dynamic-test-route")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pathPredicate").isEqualTo("/test-service/**");

        
        webTestClient.patch()
                .uri("/api/admin/routes/dynamic-test-route/status?enabled=false")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.enabled").isEqualTo(false);

        
        webTestClient.delete()
                .uri("/api/admin/routes/dynamic-test-route")
                .exchange()
                .expectStatus().isNoContent();

        
        webTestClient.get()
                .uri("/api/admin/routes/dynamic-test-route")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void rejectsInvalidRouteDestination() {
        GatewayRoute invalidRoute = new GatewayRoute();
        invalidRoute.setId("invalid-destination-route");
        invalidRoute.setRouteName("Invalid Destination");
        invalidRoute.setPathPredicate("/invalid/**");
        invalidRoute.setDestinationUri("ftp://invalid-destination"); 

        webTestClient.post()
                .uri("/api/admin/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRoute)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void adminAuthenticationTest() {
        webTestClient.post()
                .uri("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("username", "admin", "password", "admin123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.authenticated").isEqualTo(true)
                .jsonPath("$.username").isEqualTo("admin");
    }
}
