package com.mckesson.cmt.cmt_standardcode_gateway_service.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import javax.sql.DataSource;

@Configuration
public class ApplicationStartupConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupConfig.class);

    private final DataSource dataSource;

    public ApplicationStartupConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Application started - context refreshed");

        // No direct database access here - just log success
        logger.info("DataSource bean is available: {}", dataSource != null);
    }


 
}