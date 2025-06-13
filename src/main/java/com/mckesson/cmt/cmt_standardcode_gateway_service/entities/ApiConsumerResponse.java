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
@Table(name = "api_consumer_response", schema = "code-bridge")
public class ApiConsumerResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oid_master_id")
    private OidMaster oidMaster;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consumer_response", columnDefinition = "jsonb")
    private String consumerResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "version")
    private Integer version;

    @Column(name = "is_current")
    private Boolean isCurrent;

    // Default constructor
    public ApiConsumerResponse() {
    }

    // Constructor with required fields
    public ApiConsumerResponse(OidMaster oidMaster, String consumerResponse) {
        this.oidMaster = oidMaster;
        this.consumerResponse = consumerResponse;
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

    public String getConsumerResponse() {
        return consumerResponse;
    }

    public void setConsumerResponse(String consumerResponse) {
        this.consumerResponse = consumerResponse;
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
        return "ApiConsumerResponse{" +
                "id=" + id +
                ", oidMasterId=" + (oidMaster != null ? oidMaster.getId() : null) +
                ", version=" + version +
                ", isCurrent=" + isCurrent +
                '}';
    }
}