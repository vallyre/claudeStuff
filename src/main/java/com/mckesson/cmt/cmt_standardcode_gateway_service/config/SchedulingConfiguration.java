package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import com.mckesson.cmt.cmt_standardcode_gateway_service.component.ScheduledOidProcessor;
import com.mckesson.cmt.cmt_standardcode_gateway_service.services.SchedulerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Instant;

/**
 * Configuration class for dynamic scheduling setup
 */
@Configuration
@EnableScheduling
public class SchedulingConfiguration implements SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfiguration.class);

    @Autowired
    private SchedulerConfigService schedulerConfigService;

    @Autowired
    private ScheduledOidProcessor scheduledOidProcessor;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("Configuring scheduled tasks with dynamic triggers");

        // Configure dynamic OID processing task
        taskRegistrar.addTriggerTask(
                // The task to execute
                () -> {
                    try {
                        log.debug("Executing scheduled OID processing");
                        scheduledOidProcessor.scheduledProcessing();
                    } catch (Exception e) {
                        log.error("Error during scheduled OID processing", e);
                    }
                },
                // The trigger that determines when to execute the task
                triggerContext -> {
                    SchedulerConfig config = schedulerConfigService.getSchedulerConfig("oidProcessing");
                    log.debug("Next execution of OID processing using config: {}", config);

                    if (!config.isEnabled()) {
                        // If disabled, schedule far in the future (effectively disabled)
                        log.debug("OID processing is disabled, scheduling far in future");
                        return Instant.now().plusSeconds(3600 * 24 * 365); // One year from now
                    }

                    try {
                        CronTrigger trigger = new CronTrigger(config.getCronExpression());
                        return trigger.nextExecution(triggerContext);
                    } catch (IllegalArgumentException e) {
                        // If cron expression is invalid, use a default interval
                        log.error("Invalid cron expression: {}, using default interval", config.getCronExpression(), e);
                        return Instant.now().plusSeconds(3600 * 24); // 24 hours from now
                    }
                });

        // Example of another scheduled task with different configuration
        // Uncomment and modify as needed for additional scheduled tasks
        /*
         * taskRegistrar.addTriggerTask(
         * // The task to execute
         * () -> {
         * try {
         * log.debug("Executing another scheduled task");
         * // Execute another task
         * } catch (Exception e) {
         * log.error("Error during execution of another task", e);
         * }
         * },
         * // The trigger that determines when to execute the task
         * triggerContext -> {
         * SchedulerConfig config =
         * schedulerConfigService.getSchedulerConfig("anotherTask");
         * 
         * if (!config.isEnabled()) {
         * return Instant.now().plusSeconds(3600 * 24 * 365);
         * }
         * 
         * try {
         * CronTrigger trigger = new CronTrigger(config.getCronExpression());
         * return trigger.nextExecution(triggerContext);
         * } catch (IllegalArgumentException e) {
         * log.error("Invalid cron expression: {}, using default interval",
         * config.getCronExpression(), e);
         * return Instant.now().plusSeconds(3600);
         * }
         * }
         * );
         */
    }
}