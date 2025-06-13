package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "standard_codes_master", schema = "code-bridge")
public class StandardCodesMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_uuid")
    private UUID masterUuid;

    @Column(name = "resource_type")
    private String resourceType;

    private String name;
    private String version;
    private String status;
    private String title;
    private String publisher;
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String purpose;

    private LocalDateTime date;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "lastreview_date")
    private LocalDateTime lastReviewDate;

    @Column(name = "effective_start_date")
    private LocalDateTime effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDateTime effectiveEndDate;

    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;

    @Column(name = "updated_datetime")
    private LocalDateTime updatedDatetime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    private Boolean experimental;

    @OneToMany(mappedBy = "standardCodesMaster", cascade = CascadeType.ALL)
    private List<StandardCodesResponse> responses;
}