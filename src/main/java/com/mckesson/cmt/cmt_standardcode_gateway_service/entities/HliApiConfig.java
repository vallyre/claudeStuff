package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing the HLI API configuration for code-bridge
 */
@Entity
@Table(name = "hli_api_config", schema = "code-bridge")
public class HliApiConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "config_name", unique = true, nullable = false)
    private String configName;

    @Column(name = "retry_limit")
    private Integer retryLimit;

    @Column(name = "retry_interval_ms")
    private Integer retryIntervalMs;

    @Column(name = "max_response_time_ms")
    private Integer maxResponseTimeMs;

    @Column(name = "batch_size")
    private Integer batchSize;

    @Column(name = "api_base_url")
    private String apiBaseUrl;

    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "is_active")
    private Boolean isActive;

    // One-to-many relationships
    @OneToMany(mappedBy = "hliApiConfig", cascade = CascadeType.ALL)
    private List<OidMaster> oidMasters = new ArrayList<>();

    @OneToMany(mappedBy = "hliApiConfig", cascade = CascadeType.ALL)
    private List<OidBatchProcessLog> batchProcessLogs = new ArrayList<>();

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public Integer getRetryLimit() {
        return retryLimit;
    }

    public void setRetryLimit(Integer retryLimit) {
        this.retryLimit = retryLimit;
    }

    public Integer getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(Integer retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public Integer getMaxResponseTimeMs() {
        return maxResponseTimeMs;
    }

    public void setMaxResponseTimeMs(Integer maxResponseTimeMs) {
        this.maxResponseTimeMs = maxResponseTimeMs;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<OidMaster> getOidMasters() {
        return oidMasters;
    }

    public void setOidMasters(List<OidMaster> oidMasters) {
        this.oidMasters = oidMasters;
    }

    public List<OidBatchProcessLog> getBatchProcessLogs() {
        return batchProcessLogs;
    }

    public void setBatchProcessLogs(List<OidBatchProcessLog> batchProcessLogs) {
        this.batchProcessLogs = batchProcessLogs;
    }

    @Override
    public String toString() {
        return "HliApiConfig{" +
                "id=" + id +
                ", configName='" + configName + '\'' +
                ", apiBaseUrl='" + apiBaseUrl + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}