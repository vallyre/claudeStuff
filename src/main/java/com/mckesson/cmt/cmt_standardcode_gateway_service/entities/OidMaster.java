package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing the OID Master table that stores OID metadata
 */
@Entity
@Table(name = "oid_master", schema = "code-bridge")
public class OidMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oid", nullable = false)
    private String oid;

    @Column(name = "code_group_content_set")
    private String codeGroupContentSet;

    @Column(name = "code_group_content_set_version")
    private String codeGroupContentSetVersion;

    @Column(name = "code_sub_type")
    private String codeSubType;

    @Column(name = "fhir_identifier")
    private String fhirIdentifier;

    @Column(name = "hl7_uri")
    private String hl7Uri;

    @Column(name = "code")
    private String code;

    @Column(name = "code_group_name")
    private String codeGroupName;

    @Column(name = "code_group_revision_name")
    private String codeGroupRevisionName;

    @Column(name = "revision_start")
    private LocalDateTime revisionStart;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "status")
    private String status;

    @Column(name = "service_method_name")
    private String serviceMethodName;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    // Many-to-one relationship with HliApiConfig
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hli_api_config_id")
    private HliApiConfig hliApiConfig;

    // One-to-many relationships
    @OneToMany(mappedBy = "oidMaster", cascade = CascadeType.ALL)
    private List<OidHliApiResponse> apiResponses = new ArrayList<>();

    @OneToMany(mappedBy = "oidMaster", cascade = CascadeType.ALL)
    private List<ApiConsumerResponse> consumerResponses = new ArrayList<>();

    @OneToMany(mappedBy = "oidMaster", cascade = CascadeType.ALL)
    private List<ApiConsumerRequestLog> requestLogs = new ArrayList<>();

    @OneToMany(mappedBy = "oidMaster", cascade = CascadeType.ALL)
    private List<ApiConsumerOidRequestLog> oidRequestLogs = new ArrayList<>();

    // Default constructor
    public OidMaster() {
    }

    // Constructor with required fields
    public OidMaster(String oid, String codeGroupContentSet, String codeGroupContentSetVersion,
            String code, String codeGroupName, HliApiConfig hliApiConfig) {
        this.oid = oid;
        this.codeGroupContentSet = codeGroupContentSet;
        this.codeGroupContentSetVersion = codeGroupContentSetVersion;
        this.code = code;
        this.codeGroupName = codeGroupName;
        this.hliApiConfig = hliApiConfig;
        this.isActive = true;
        this.status = "ACTIVE";
        this.createdDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getCodeGroupContentSet() {
        return codeGroupContentSet;
    }

    public void setCodeGroupContentSet(String codeGroupContentSet) {
        this.codeGroupContentSet = codeGroupContentSet;
    }

    public String getCodeGroupContentSetVersion() {
        return codeGroupContentSetVersion;
    }

    public void setCodeGroupContentSetVersion(String codeGroupContentSetVersion) {
        this.codeGroupContentSetVersion = codeGroupContentSetVersion;
    }

    public String getCodeSubType() {
        return codeSubType;
    }

    public void setCodeSubType(String codeSubType) {
        this.codeSubType = codeSubType;
    }

    public String getFhirIdentifier() {
        return fhirIdentifier;
    }

    public void setFhirIdentifier(String fhirIdentifier) {
        this.fhirIdentifier = fhirIdentifier;
    }

    public String getHl7Uri() {
        return hl7Uri;
    }

    public void setHl7Uri(String hl7Uri) {
        this.hl7Uri = hl7Uri;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeGroupName() {
        return codeGroupName;
    }

    public void setCodeGroupName(String codeGroupName) {
        this.codeGroupName = codeGroupName;
    }

    public String getCodeGroupRevisionName() {
        return codeGroupRevisionName;
    }

    public void setCodeGroupRevisionName(String codeGroupRevisionName) {
        this.codeGroupRevisionName = codeGroupRevisionName;
    }

    public LocalDateTime getRevisionStart() {
        return revisionStart;
    }

    public void setRevisionStart(LocalDateTime revisionStart) {
        this.revisionStart = revisionStart;
    }

    public HliApiConfig getHliApiConfig() {
        return hliApiConfig;
    }

    public void setHliApiConfig(HliApiConfig hliApiConfig) {
        this.hliApiConfig = hliApiConfig;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public List<OidHliApiResponse> getApiResponses() {
        return apiResponses;
    }

    public void setApiResponses(List<OidHliApiResponse> apiResponses) {
        this.apiResponses = apiResponses;
    }

    public List<ApiConsumerResponse> getConsumerResponses() {
        return consumerResponses;
    }

    public void setConsumerResponses(List<ApiConsumerResponse> consumerResponses) {
        this.consumerResponses = consumerResponses;
    }

    public List<ApiConsumerRequestLog> getRequestLogs() {
        return requestLogs;
    }

    public void setRequestLogs(List<ApiConsumerRequestLog> requestLogs) {
        this.requestLogs = requestLogs;
    }

    public List<ApiConsumerOidRequestLog> getOidRequestLogs() {
        return oidRequestLogs;
    }

    public void setOidRequestLogs(List<ApiConsumerOidRequestLog> oidRequestLogs) {
        this.oidRequestLogs = oidRequestLogs;
    }

    public String getServiceMethodName() {
        return serviceMethodName;
    }

    public void setServiceMethodName(String serviceMethodName) {
        this.serviceMethodName = serviceMethodName;
    }

    @Override
    public String toString() {
        return "OidMaster{" +
                "id=" + id +
                ", oid='" + oid + '\'' +
                ", code='" + code + '\'' +
                ", codeGroupName='" + codeGroupName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}