package com.mckesson.cmt.cmt_standardcode_gateway_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableJpaAuditing
@EnableMethodSecurity
@SpringBootApplication
@EnableScheduling // Add this annotation to enable scheduling
@ComponentScan(basePackages = "com.mckesson.cmt.cmt_standardcode_gateway_service")
@EnableJpaRepositories(basePackages = "com.mckesson.cmt.cmt_standardcode_gateway_service.repository")
@EntityScan(basePackages = "com.mckesson.cmt.cmt_standardcode_gateway_service.entities")
public class CmtStandardcodeGatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmtStandardcodeGatewayServiceApplication.class, args);
    }

    /**
     * Configure CORS for development purposes.
     * This should be restricted in production.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
}