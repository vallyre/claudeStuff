package com.mckesson.cmt.cmt_standardcode_gateway_service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class StandardCodesResponseDto {
    private UUID uuid;
    private String apiResponse;
    private Integer responseTimeMs;
    private Integer httpStatusCode;
    private LocalDateTime createdAt;
    private Integer version;
    private Boolean isCurrent;
    private LocalDateTime effectiveStartDate;
    private LocalDateTime effectiveEndDate;
}
