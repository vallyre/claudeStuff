package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

/**
 * Data class to hold scheduler configuration
 */
public class SchedulerConfig {
    private String jobName;
    private String cronExpression;
    private boolean enabled;

    public SchedulerConfig() {
    }

    public SchedulerConfig(String jobName, String cronExpression, boolean enabled) {
        this.jobName = jobName;
        this.cronExpression = cronExpression;
        this.enabled = enabled;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "SchedulerConfig{" +
                "jobName='" + jobName + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}