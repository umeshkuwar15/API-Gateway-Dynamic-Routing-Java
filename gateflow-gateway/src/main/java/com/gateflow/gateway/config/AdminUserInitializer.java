package com.gateflow.gateway.config;

import com.gateflow.gateway.model.AdminUser;
import com.gateflow.gateway.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        adminUserRepository.findByUsername("admin")
                .switchIfEmpty(Mono.defer(() -> {
                    AdminUser admin = new AdminUser();
                    admin.setUsername("admin");
                    admin.setPasswordHash(passwordEncoder.encode("admin123"));
                    admin.setFullName("GateFlow Administrator");
                    admin.setEmail("admin@gateflow.local");
                    admin.setRole("ROLE_ADMIN");
                    admin.setEnabled(true);
                    admin.setCreatedAt(LocalDateTime.now());
                    admin.setUpdatedAt(LocalDateTime.now());
                    log.info("Creating default admin user in MySQL...");
                    return adminUserRepository.save(admin);
                }))
                .flatMap(admin -> {
                    if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
                        log.info("Updating admin user password hash with standard BCrypt...");
                        admin.setPasswordHash(passwordEncoder.encode("admin123"));
                        return adminUserRepository.save(admin);
                    }
                    return Mono.just(admin);
                })
                .doOnSuccess(admin -> log.info("Admin user authentication verified: username='{}'", admin.getUsername()))
                .doOnError(err -> log.warn("AdminUserInitializer warning: {}", err.getMessage()))
                .subscribe();
    }
}
