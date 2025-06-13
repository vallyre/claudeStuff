package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class DatabasePropertiesLoader {

    private static final Logger logger = LoggerFactory.getLogger(DatabasePropertiesLoader.class);

    @Value("${spring.datasource.url:MISSING}")
    private String dbUrl;

    @Value("${spring.datasource.username:MISSING}")
    private String dbUsername;

    @Value("${spring.datasource.password:MISSING}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name:MISSING}")
    private String dbDriverClassName;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Checking database properties...");
        logger.info("Database URL: {}", dbUrl);
        logger.info("Database Username: {}", dbUsername);
        // Print only first few characters of the password for security
        logger.info("Database Password first 3 chars: {}",
                dbPassword != null && dbPassword.length() > 3 ? dbPassword.substring(0, 3) + "..." : "[EMPTY]");
        logger.info("Database Driver: {}", dbDriverClassName);

        if ("MISSING".equals(dbUrl) || "MISSING".equals(dbUsername) || "MISSING".equals(dbDriverClassName)) {
            logger.error("DATABASE PROPERTIES ARE MISSING - Check your application.properties file");
        }
    }
}
