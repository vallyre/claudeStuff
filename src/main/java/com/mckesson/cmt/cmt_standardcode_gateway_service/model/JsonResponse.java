package com.mckesson.cmt.cmt_standardcode_gateway_service.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonResponse {
    private String responseId;
    private String requestId;
    private ZonedDateTime timestamp;
    private String status;
    private List<JsonNode> responses;
    private ErrorResponse error;
}