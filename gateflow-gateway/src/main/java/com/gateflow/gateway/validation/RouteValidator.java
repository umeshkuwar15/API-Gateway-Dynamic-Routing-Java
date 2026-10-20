package com.gateflow.gateway.validation;

import com.gateflow.gateway.model.GatewayRoute;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class RouteValidator {

    private static final Pattern ROUTE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,100}$");
    private static final List<String> VALID_METHODS = Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");

    public void validate(GatewayRoute route) {
        if (route == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route configuration cannot be null");
        }

        
        if (route.getId() == null || !ROUTE_ID_PATTERN.matcher(route.getId().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Route ID must be 3-100 characters consisting of letters, digits, underscores, or hyphens");
        }

        
        if (route.getRouteName() == null || route.getRouteName().trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route name cannot be blank");
        }

        
        if (route.getPathPredicate() == null || !route.getPathPredicate().trim().startsWith("/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path predicate must start with a leading slash '/' (e.g. /users/**)");
        }
        String path = route.getPathPredicate().trim();
        if (path.startsWith("/api/admin") || path.startsWith("/actuator")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Path predicate conflicts with internal management routes: " + path);
        }

        
        if (route.getDestinationUri() == null || route.getDestinationUri().trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination URI cannot be blank");
        }
        try {
            URI uri = URI.create(route.getDestinationUri().trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("lb"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Destination URI scheme must be http, https, or lb. Found: " + scheme);
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination URI must have a valid host");
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Destination URI syntax: " + e.getMessage());
        }

        
        if (route.getHttpMethods() != null && !route.getHttpMethods().trim().isBlank()) {
            String[] methods = route.getHttpMethods().split(",");
            for (String m : methods) {
                String clean = m.trim().toUpperCase();
                if (!clean.equals("ALL") && !VALID_METHODS.contains(clean)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Unsupported HTTP method in route configuration: " + clean);
                }
            }
        }
    }
}
