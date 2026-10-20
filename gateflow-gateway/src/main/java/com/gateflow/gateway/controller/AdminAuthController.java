package com.gateflow.gateway.controller;

import com.gateflow.gateway.model.AdminUser;
import com.gateflow.gateway.repository.AdminUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthController(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required"));
        }

        return adminUserRepository.findByUsername(username)
                .filter(AdminUser::isEnabled)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .map(user -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("id", user.getId());
                    response.put("username", user.getUsername());
                    response.put("fullName", user.getFullName());
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole());
                    response.put("authenticated", true);
                    return ResponseEntity.ok(response);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")));
    }
}
