package com.crm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3 Configuration
 * Provides interactive API documentation with Basic Authentication support
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define Basic Auth security scheme
        SecurityScheme basicAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .name("Basic Authentication")
                .description("Enter your username and password");

        // Security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("basicAuth");

        return new OpenAPI()
                // API Information
                .info(new Info()
                        .title("CRM System REST API")
                        .version("1.0.0")
                        .description("Customer Relationship Management System - Enterprise-level Backend REST API\n\n" +
                                "### Features:\n" +
                                "- **Customer Management**: Full CRUD operations for managing customers\n" +
                                "- **Commercial Offers**: Create and track business proposals\n" +
                                "- **Task Management**: Organize tasks linked to customers and offers\n" +
                                "- **User Management**: Role-based access control (ADMIN, MANAGER, USER)\n" +
                                "- **Security**: Basic Authentication with Spring Security\n\n" +
                                "### Authentication:\n" +
                                "Use one of the following test credentials:\n" +
                                "- **ADMIN**: username=`admin`, password=`admin123` (Full access)\n" +
                                "- **MANAGER**: username=`manager`, password=`manager123` (Create/Edit)\n" +
                                "- **USER**: username=`user`, password=`user123` (Read-only)")
                        .contact(new Contact()
                                .name("CRM Development Team")
                                .email("support@crm.example.com")
                                .url("https://github.com/your-repo/crm"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))

                // Servers
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.crm.example.com")
                                .description("Production Server (Example)")
                ))

                // Security
                .components(new Components()
                        .addSecuritySchemes("basicAuth", basicAuth))
                .addSecurityItem(securityRequirement);
    }
}