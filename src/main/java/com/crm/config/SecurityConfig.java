package com.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1) Дозволяємо H2-консоль без авторизації
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().permitAll()
                )
                // 2) Вимикаємо CSRF для коректної роботи консолі
                .csrf(csrf -> csrf.disable())
                // 3) Дозволяємо відображення в <iframe> (frameOptions.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )
                // 4) Стандартна форма логіну
                .formLogin(withDefaults());

        return http.build();
    }

}
