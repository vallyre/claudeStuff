package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;



import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "api_consumer_request_type")
public class ApiConsumerRequestType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_name", unique = true)
    private String typeName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "requires_oid")
    private Boolean requiresOid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_params", columnDefinition = "jsonb")
    private String additionalParams;

    @Column(name = "cache_ttl_seconds")
    private Integer cacheTtlSeconds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive;

    // One-to-many relationship
    @OneToMany(mappedBy = "requestType", cascade = CascadeType.ALL)
    private List<ApiConsumerRequestLog> requestLogs = new ArrayList<>();

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequiresOid() {
        return requiresOid;
    }

    public void setRequiresOid(Boolean requiresOid) {
        this.requiresOid = requiresOid;
    }

    public String getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(String additionalParams) {
        this.additionalParams = additionalParams;
    }

    public Integer getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(Integer cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<ApiConsumerRequestLog> getRequestLogs() {
        return requestLogs;
    }

    public void setRequestLogs(List<ApiConsumerRequestLog> requestLogs) {
        this.requestLogs = requestLogs;
    }
}
