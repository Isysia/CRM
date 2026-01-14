package com.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration - TEMPORARY PERMISSIVE MODE
 * Allows all requests without authentication for development.
 * Will be replaced with proper security later.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests
                );

        return http.build();
    }
}