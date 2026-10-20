INSERT INTO admin_users (username, password_hash, full_name, email, role, enabled)
VALUES (
    'admin',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'GateFlow Administrator',
    'admin@gateflow.local',
    'ROLE_ADMIN',
    TRUE
) ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO gateway_routes (id, route_name, path_predicate, destination_uri, http_methods, strip_prefix_parts, order_index, enabled, description)
VALUES 
(
    'user-service-route',
    'User Management Service',
    '/users/**',
    'http://localhost:8081',
    'GET,POST,PUT,DELETE,PATCH',
    0,
    1,
    TRUE,
    'Dynamic route forwarding /users/** traffic to gateflow-user-service on port 8081'
),
(
    'product-service-route',
    'Product Catalog Service',
    '/products/**',
    'http://localhost:8082',
    'GET,POST,PUT,DELETE,PATCH',
    0,
    2,
    TRUE,
    'Dynamic route forwarding /products/** traffic to gateflow-product-service on port 8082'
),
(
    'order-service-route',
    'Order Processing Service',
    '/orders/**',
    'http://localhost:8083',
    'GET,POST,PUT,DELETE,PATCH',
    0,
    3,
    TRUE,
    'Dynamic route forwarding /orders/** traffic to gateflow-order-service on port 8083'
)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO backend_services (service_id, service_name, base_url, health_endpoint, service_type, enabled, health_status)
VALUES
(
    'user-service',
    'GateFlow User Service',
    'http://localhost:8081',
    '/api/health',
    'REST',
    TRUE,
    'UNKNOWN'
),
(
    'product-service',
    'GateFlow Product Service',
    'http://localhost:8082',
    '/api/health',
    'REST',
    TRUE,
    'UNKNOWN'
),
(
    'order-service',
    'GateFlow Order Service',
    'http://localhost:8083',
    '/api/health',
    'REST',
    TRUE,
    'UNKNOWN'
)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO users (username, email, first_name, last_name, department, active)
VALUES 
('jdoe', 'john.doe@example.com', 'John', 'Doe', 'Engineering', TRUE),
('asmith', 'alice.smith@example.com', 'Alice', 'Smith', 'Marketing', TRUE),
('bwayne', 'bruce.wayne@example.com', 'Bruce', 'Wayne', 'Executive', TRUE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO products (name, description, price, stock_quantity, category)
VALUES
('Mechanical Keyboard', 'RGB Tenkeyless Mechanical Keyboard with Cherry MX Brown switches', 89.99, 50, 'Electronics'),
('Wireless Ergonomic Mouse', '2.4GHz wireless mouse with hyper-fast scroll wheel', 49.99, 120, 'Electronics'),
('Noise-Cancelling Headphones', 'Active noise cancellation with 30-hour battery life', 199.99, 35, 'Audio')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO orders (customer_id, order_status, total_amount, shipping_address)
VALUES
(1, 'CONFIRMED', 139.98, '123 Tech Boulevard, Silicon Valley, CA'),
(2, 'SHIPPED', 199.99, '456 Innovation Way, Austin, TX')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;
