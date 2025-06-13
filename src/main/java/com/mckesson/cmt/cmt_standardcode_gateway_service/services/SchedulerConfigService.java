package com.mckesson.cmt.cmt_standardcode_gateway_service.services;

import com.mckesson.cmt.cmt_standardcode_gateway_service.config.SchedulerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to retrieve scheduler configuration from database
 */
@Service
public class SchedulerConfigService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfigService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retrieve scheduler configuration for a specific job
     * Results are cached to avoid frequent database calls
     *
     * @param jobName The name of the job to retrieve configuration for
     * @return SchedulerConfig containing job configuration
     */
    @Cacheable(value = "schedulerConfig", key = "#jobName")
    public SchedulerConfig getSchedulerConfig(String jobName) {
        log.debug("Fetching scheduler configuration for job: {}", jobName);

        try {
            return jdbcTemplate.queryForObject(
                    "SELECT job_name, cron_expression, enabled FROM scheduler_config WHERE job_name = ?",
                    (rs, rowNum) -> new SchedulerConfig(
                            rs.getString("job_name"),
                            rs.getString("cron_expression"),
                            rs.getBoolean("enabled")),
                    jobName);
        } catch (BadSqlGrammarException e) {
            // Table might not exist yet
            log.warn("scheduler_config table does not exist yet: {}", e.getMessage());
            return new SchedulerConfig(jobName, "0 0 2 * * ?", false); // Default values
        } catch (EmptyResultDataAccessException e) {
            log.warn("No scheduler configuration found for job: {}", jobName);
            return new SchedulerConfig(jobName, "0 0 2 * * ?", false); // Default values
        }
    }

    /**
     * Update the configuration for a specific job
     *
     * @param jobName        The name of the job to update
     * @param cronExpression The new cron expression
     * @param enabled        Whether the job should be enabled
     * @return true if the update was successful, false otherwise
     */
    @Transactional
    public boolean updateSchedulerConfig(String jobName, String cronExpression, boolean enabled) {
        log.info("Updating scheduler configuration for job: {}", jobName);

        int rowsAffected = jdbcTemplate.update(
                "UPDATE scheduler_config SET cron_expression = ?, enabled = ?, last_updated = CURRENT_TIMESTAMP " +
                        "WHERE job_name = ?",
                cronExpression, enabled, jobName);

        if (rowsAffected == 0) {
            // If no rows were affected, try to insert a new configuration
            try {
                jdbcTemplate.update(
                        "INSERT INTO scheduler_config (job_name, cron_expression, enabled, description, last_updated) "
                                +
                                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                        jobName, cronExpression, enabled, "Dynamically added job");
                return true;
            } catch (Exception e) {
                log.error("Failed to insert new scheduler configuration for job: {}", jobName, e);
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a specific job exists in the configuration
     *
     * @param jobName The name of the job to check
     * @return true if the job exists, false otherwise
     */
    public boolean jobExists(String jobName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scheduler_config WHERE job_name = ?",
                Integer.class,
                jobName);
        return count != null && count > 0;
    }
}