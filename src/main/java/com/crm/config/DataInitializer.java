package com.crm.config;

import com.crm.security.model.Role;
import com.crm.security.model.User;
import com.crm.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Initialize default users for testing
 * This runs once at application startup
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Skip if users already exist
            if (userRepository.count() > 0) {
                log.info("Users already initialized. Skipping...");
                return;
            }

            log.info("Initializing default users...");

            // Create ADMIN user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@crm.com");
            admin.setRoles(Set.of(Role.ROLE_ADMIN));
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);
            userRepository.save(admin);
            log.info("Created ADMIN user: username=admin, password=admin123");

            // Create MANAGER user
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setEmail("manager@crm.com");
            manager.setRoles(Set.of(Role.ROLE_MANAGER));
            manager.setEnabled(true);
            manager.setAccountNonLocked(true);
            userRepository.save(manager);
            log.info("Created MANAGER user: username=manager, password=manager123");

            // Create USER (read-only)
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@crm.com");
            user.setRoles(Set.of(Role.ROLE_USER));
            user.setEnabled(true);
            user.setAccountNonLocked(true);
            userRepository.save(user);
            log.info("Created USER (read-only): username=user, password=user123");

            log.info("Default users initialized successfully!");
            log.info("=== Test Credentials ===");
            log.info("ADMIN    -> username: admin,   password: admin123");
            log.info("MANAGER  -> username: manager, password: manager123");
            log.info("USER     -> username: user,    password: user123");
            log.info("========================");
        };
    }
}