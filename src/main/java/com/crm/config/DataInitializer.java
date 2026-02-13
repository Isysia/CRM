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

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setEmail("admin@crm.com");
            admin.setRoles(Set.of("ROLE_ADMIN"));  // ← ДОДАЙ ROLE_
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);
            userRepository.save(admin);
            log.info("Created ADMIN user: username=admin, password=password");

            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("password"));
            manager.setEmail("manager@crm.com");
            manager.setRoles(Set.of("ROLE_MANAGER"));  // ← ДОДАЙ ROLE_
            manager.setEnabled(true);
            manager.setAccountNonLocked(true);
            userRepository.save(manager);
            log.info("Created MANAGER user: username=manager, password=password");

            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("password"));
            user.setEmail("user@crm.com");
            user.setRoles(Set.of("ROLE_USER"));  // ← ДОДАЙ ROLE_
            user.setEnabled(true);
            user.setAccountNonLocked(true);
            userRepository.save(user);
            log.info("Created USER (read-only): username=user, password=password");

            log.info("Default users initialized successfully!");
        };
    }
}