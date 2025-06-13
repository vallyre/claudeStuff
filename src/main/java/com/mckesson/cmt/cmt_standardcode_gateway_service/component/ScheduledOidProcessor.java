package com.mckesson.cmt.cmt_standardcode_gateway_service.component;

import com.mckesson.cmt.cmt_standardcode_gateway_service.config.SchedulerConfig;
import com.mckesson.cmt.cmt_standardcode_gateway_service.services.OidProcessingService;
import com.mckesson.cmt.cmt_standardcode_gateway_service.services.SchedulerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component that handles scheduled OID processing
 * Note: This class does not use @Scheduled annotations as scheduling is
 * configured dynamically in SchedulingConfiguration
 */
@Component
public class ScheduledOidProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduledOidProcessor.class);

    @Autowired
    private OidProcessingService oidProcessingService;

    @Autowired
    private SchedulerConfigService schedulerConfigService;

    /**
     * Process all pending OIDs
     * This method is called by the scheduler based on the configuration
     */
    public void scheduledProcessing() {
        log.info("Checking whether scheduled OID processing is enabled");

        SchedulerConfig config = schedulerConfigService.getSchedulerConfig("oidProcessing");

        if (!config.isEnabled()) {
            log.info("Scheduled OID processing is disabled");
            return;
        }

        log.info("Starting scheduled OID processing with cron: {}", config.getCronExpression());

        try {
            oidProcessingService.processAllPendingOids();
            log.info("Completed scheduled OID processing");
        } catch (Exception e) {
            log.error("Error during scheduled OID processing", e);
        }
    }

    /**
     * Run OID processing manually, regardless of scheduler configuration
     * Useful for admin-triggered processing or testing
     */
    public void manualProcessing() {
        log.info("Starting manual OID processing");

        try {
            oidProcessingService.processAllPendingOids();
            log.info("Completed manual OID processing");
        } catch (Exception e) {
            log.error("Error during manual OID processing", e);
            throw e;
        }
    }
}