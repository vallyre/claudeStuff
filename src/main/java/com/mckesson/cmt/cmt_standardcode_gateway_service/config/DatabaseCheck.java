package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Component to validate database connection during application startup
 */
@Component
@Order(1)
public class DatabaseCheck implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCheck.class);

    private final DataSource dataSource;

    @Autowired
    public DatabaseCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {

            logger.info("Verifying database connection...");
            ResultSet resultSet = statement.executeQuery("SELECT version()");

            if (resultSet.next()) {
                String dbVersion = resultSet.getString(1);
                logger.info("✓ Database connected successfully - PostgreSQL version: {}", dbVersion);

                // Verify schema exists and is accessible
                try {
                    ResultSet schemaResult = statement.executeQuery(
                            "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = 'code-bridge')");

                    if (schemaResult.next() && schemaResult.getBoolean(1)) {
                        logger.info("✓ Schema 'code-bridge' exists and is accessible");
                    } else {
                        logger.warn("✗ Schema 'code-bridge' does not exist or is not accessible");
                    }
                } catch (Exception e) {
                    logger.warn("Could not verify schema existence: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("✗ Failed to connect to the database: {}", e.getMessage());
            logger.error("Please check your database configuration and ensure the PostgreSQL server is running.");
        }
    }
}