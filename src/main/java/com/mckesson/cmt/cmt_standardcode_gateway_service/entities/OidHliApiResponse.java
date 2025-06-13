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
@Table(name = "oid_hli_api_response", schema = "code-bridge")
public class OidHliApiResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oid_master_id")
    private OidMaster oidMaster;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_response", columnDefinition = "jsonb")
    private String apiResponse;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "version")
    private Integer version;

    @Column(name = "is_current")
    private Boolean isCurrent;

    // Default constructor
    public OidHliApiResponse() {
    }

    // Constructor with required fields
    public OidHliApiResponse(OidMaster oidMaster, String apiResponse, Integer httpStatusCode, Integer responseTimeMs) {
        this.oidMaster = oidMaster;
        this.apiResponse = apiResponse;
        this.httpStatusCode = httpStatusCode;
        this.responseTimeMs = responseTimeMs;
        this.createdAt = LocalDateTime.now();
        this.version = 1;
        this.isCurrent = true;
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

    public String getApiResponse() {
        return apiResponse;
    }

    public void setApiResponse(String apiResponse) {
        this.apiResponse = apiResponse;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    @Override
    public String toString() {
        return "OidHliApiResponse{" +
                "id=" + id +
                ", oidMasterId=" + (oidMaster != null ? oidMaster.getId() : null) +
                ", httpStatusCode=" + httpStatusCode +
                ", version=" + version +
                ", isCurrent=" + isCurrent +
                '}';
    }
}