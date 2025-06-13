package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "api_consumer_oid_request_log", schema = "code-bridge")
public class ApiConsumerOidRequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oid_master_id")
    private OidMaster oidMaster;

    @Column(name = "request_timestamp")
    private LocalDateTime requestTimestamp;

    @Column(name = "response_timestamp")
    private LocalDateTime responseTimestamp;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_headers", columnDefinition = "jsonb")
    private String requestHeaders;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_params", columnDefinition = "jsonb")
    private String requestParams;

    @Column(name = "success")
    private Boolean success;

    // Default constructor
    public ApiConsumerOidRequestLog() {
    }

    // Constructor with required fields
    public ApiConsumerOidRequestLog(OidMaster oidMaster, String requestHeaders, String requestParams) {
        this.oidMaster = oidMaster;
        this.requestHeaders = requestHeaders;
        this.requestParams = requestParams;
        this.requestTimestamp = LocalDateTime.now();
        this.retryCount = 0;
        this.success = false;
    }

    // Method to record response
    public void recordResponse(Integer httpStatusCode, Integer responseTimeMs, Boolean success) {
        this.httpStatusCode = httpStatusCode;
        this.responseTimeMs = responseTimeMs;
        this.success = success;
        this.responseTimestamp = LocalDateTime.now();
    }

    // Method to increment retry count
    public void incrementRetryCount() {
        if (this.retryCount == null) {
            this.retryCount = 1;
        } else {
            this.retryCount++;
        }
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OidMaster getOidMaster() {
        return oidMaster;
    }

    public void setOidMaster(OidMaster oidMaster) {
        this.oidMaster = oidMaster;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ApiConsumerOidRequestLog{" +
                "id=" + id +
                ", oidMasterId=" + (oidMaster != null ? oidMaster.getId() : null) +
                ", httpStatusCode=" + httpStatusCode +
                ", retryCount=" + retryCount +
                ", success=" + success +
                '}';
    }
}