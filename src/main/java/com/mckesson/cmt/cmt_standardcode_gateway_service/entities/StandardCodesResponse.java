package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "standard_codes_responses", schema = "code-bridge")
public class StandardCodesResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private UUID versionUuid;

    @Column(name = "master_uuid")
    private UUID masterUuid;

    private String version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_codes_master_id")
    private StandardCodesMaster standardCodesMaster;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_response", columnDefinition = "jsonb")
    private String apiResponse;

    private LocalDateTime effectiveStartDate;
    private LocalDateTime effectiveEndDate;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private String createdBy;
    private String updatedBy;
}