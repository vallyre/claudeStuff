package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "api_consumer_request_log", schema = "code-bridge")
public class ApiConsumerRequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "request_timestamp")
    private LocalDateTime requestTimestamp;

    @Column(name = "response_timestamp")
    private LocalDateTime responseTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id")
    private ApiConsumerRequestType requestType;

    @Column(name = "request_id")
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oid_master_id")
    private OidMaster oidMaster;

    @Column(name = "request_ip")
    private String requestIp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_params", columnDefinition = "jsonb")
    private String additionalParams;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "cache_hit")
    private Boolean cacheHit;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    // Default constructor
    public ApiConsumerRequestLog() {
    }

    // Constructor with required fields
    public ApiConsumerRequestLog(ApiConsumerRequestType requestType, String requestId,
            OidMaster oidMaster, String requestIp) {
        this.requestType = requestType;
        this.requestId = requestId;
        this.oidMaster = oidMaster;
        this.requestIp = requestIp;
        this.requestTimestamp = LocalDateTime.now();
        this.cacheHit = false;
    }

    // Method to record response
    public void recordResponse(Integer httpStatusCode, Integer responseTimeMs, Boolean cacheHit) {
        this.httpStatusCode = httpStatusCode;
        this.responseTimeMs = responseTimeMs;
        this.cacheHit = cacheHit;
        this.responseTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public ApiConsumerRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(ApiConsumerRequestType requestType) {
        this.requestType = requestType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public OidMaster getOidMaster() {
        return oidMaster;
    }

    public void setOidMaster(OidMaster oidMaster) {
        this.oidMaster = oidMaster;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public String getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(String additionalParams) {
        this.additionalParams = additionalParams;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Boolean getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(Boolean cacheHit) {
        this.cacheHit = cacheHit;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ApiConsumerRequestLog{" +
                "id=" + id +
                ", requestId='" + requestId + '\'' +
                ", oidMasterId=" + (oidMaster != null ? oidMaster.getId() : null) +
                ", requestTypeId=" + (requestType != null ? requestType.getId() : null) +
                ", httpStatusCode=" + httpStatusCode +
                ", responseTimeMs=" + responseTimeMs +
                ", cacheHit=" + cacheHit +
                '}';
    }
}