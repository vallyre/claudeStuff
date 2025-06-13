package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration class for JPA settings and entity management
 */
@Configuration
@EnableTransactionManagement
public class JpaConfig {

    private static final Logger logger = LoggerFactory.getLogger(JpaConfig.class);

    @Value("${spring.jpa.properties.hibernate.default_schema:code-bridge}")
    private String defaultSchema;

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String hbm2ddlAuto;

    @Value("${spring.jpa.show-sql:true}")
    private String showSql;

    @Value("${spring.jpa.properties.hibernate.format_sql:true}")
    private String formatSql;

    /**
     * Configures the EntityManagerFactory with proper entity scanning and Hibernate
     * settings
     * 
     * @param dataSource The configured DataSource
     * @return The configured EntityManagerFactory
     */
    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        logger.info("Configuring EntityManagerFactory with schema: {}", defaultSchema);

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);

        // Important: Make sure this matches your entity package exactly
        em.setPackagesToScan("com.mckesson.cmt.cmt_standardcode_gateway_service.entities");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.default_schema", defaultSchema);
        properties.setProperty("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.setProperty("hibernate.show_sql", showSql);
        properties.setProperty("hibernate.format_sql", formatSql);
        em.setJpaProperties(properties);

        return em;
    }

    /**
     * Configures the JPA transaction manager
     * 
     * @param entityManagerFactory The configured EntityManagerFactory
     * @return The configured PlatformTransactionManager
     */
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}