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
            if (userRepository.count() > 0) {
                log.info("Users already initialized. Skipping...");
                return;
            }

            log.info("Initializing default users...");

            // Create ADMIN user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setEmail("admin@crm.com");
            admin.setRoles(Set.of(Role.ROLE_ADMIN.name()));
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);
            userRepository.save(admin);
            log.info("Created ADMIN user: username=admin, password=password");

            // Create MANAGER user
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("password"));
            manager.setEmail("manager@crm.com");
            manager.setRoles(Set.of(Role.ROLE_MANAGER.name()));
            manager.setEnabled(true);
            manager.setAccountNonLocked(true);
            userRepository.save(manager);
            log.info("Created MANAGER user: username=manager, password=password");

            // Create USER (read-only)
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("password"));
            user.setEmail("user@crm.com");
            user.setRoles(Set.of(Role.ROLE_USER.name()));
            user.setEnabled(true);
            user.setAccountNonLocked(true);
            userRepository.save(user);
            log.info("Created USER (read-only): username=user, password=password");

            log.info("Default users initialized successfully!");
            log.info("=== Test Credentials ===");
            log.info("ADMIN    -> username: admin,   password: password");
            log.info("MANAGER  -> username: manager, password: password");
            log.info("USER     -> username: user,    password: password");
            log.info("========================");
        };
    }
}