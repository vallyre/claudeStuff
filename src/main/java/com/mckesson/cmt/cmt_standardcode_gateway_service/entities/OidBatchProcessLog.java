package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "oid_batch_process_log")
public class OidBatchProcessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "batch_id")
    private String batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hli_api_config_id")
    private HliApiConfig hliApiConfig;

    @Column(name = "batch_start_time")
    private LocalDateTime batchStartTime;

    @Column(name = "batch_end_time")
    private LocalDateTime batchEndTime;

    @Column(name = "total_oids")
    private Integer totalOids;

    @Column(name = "successful_oids")
    private Integer successfulOids;

    @Column(name = "failed_oids")
    private Integer failedOids;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public HliApiConfig getHliApiConfig() {
        return hliApiConfig;
    }

    public void setHliApiConfig(HliApiConfig hliApiConfig) {
        this.hliApiConfig = hliApiConfig;
    }

    public LocalDateTime getBatchStartTime() {
        return batchStartTime;
    }

    public void setBatchStartTime(LocalDateTime batchStartTime) {
        this.batchStartTime = batchStartTime;
    }

    public LocalDateTime getBatchEndTime() {
        return batchEndTime;
    }

    public void setBatchEndTime(LocalDateTime batchEndTime) {
        this.batchEndTime = batchEndTime;
    }

    public Integer getTotalOids() {
        return totalOids;
    }

    public void setTotalOids(Integer totalOids) {
        this.totalOids = totalOids;
    }

    public Integer getSuccessfulOids() {
        return successfulOids;
    }

    public void setSuccessfulOids(Integer successfulOids) {
        this.successfulOids = successfulOids;
    }

    public Integer getFailedOids() {
        return failedOids;
    }

    public void setFailedOids(Integer failedOids) {
        this.failedOids = failedOids;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
