package com.mckesson.cmt.cmt_standardcode_gateway_service.controllers;

import com.mckesson.cmt.cmt_standardcode_gateway_service.model.JsonResponse;
import com.mckesson.cmt.cmt_standardcode_gateway_service.model.MasterUuidRequest;
import com.mckesson.cmt.cmt_standardcode_gateway_service.services.StandardCodesResponseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/standard-codes/responses")
@Tag(name = "Standard Codes Responses", description = "API endpoints for searching and retrieving standard code responses by UUIDs. "
        +
        "This service allows clients to search for multiple standard codes in a single request " +
        "and receive consolidated responses.")
public class StandardCodesResponseController {

    private final StandardCodesResponseService responseService;

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search responses by UUIDs", description = "Retrieves standard code responses using a list of master UUIDs. "
            +
            "The service searches both master and version UUIDs to provide comprehensive results. " +
            "Returns consolidated JSON responses for all matching UUIDs.", security = @SecurityRequirement(name = "oauth2"))
    @RequestBody(description = "Request containing UUIDs to search for", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MasterUuidRequest.class), examples = @ExampleObject(name = "Search Request Example", summary = "Example request with multiple UUIDs", value = """
            {
              "operation": "SEARCH",
              "parameters": {
                "uuids": [
                  "550e8400-e29b-41d4-a716-446655440001",
                  "550e8400-e29b-41d4-a716-446655440002",
                  "550e8400-e29b-41d4-a716-446655440003"
                ]
              },
              "priority": "HIGH"
            }
            """)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved responses", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JsonResponse.class), examples = @ExampleObject(name = "Successful Response", summary = "Example successful response with standard code data", value = """
                    {
                      "responseId": "resp-123e4567-e89b-12d3-a456-426614174000",
                      "requestId": "req-123e4567-e89b-12d3-a456-426614174000",
                      "timestamp": "2025-01-28T10:30:00Z",
                      "status": "SUCCESS",
                      "responses": [
                        {
                          "resourceType": "ValueSet",
                          "id": "digital-access-codes",
                          "url": "http://example.org/fhir/ValueSet/digital-access",
                          "version": "1.0.0",
                          "name": "DigitalAccessCodes",
                          "title": "Digital Access Assessment Codes",
                          "status": "active",
                          "compose": {
                            "include": [
                              {
                                "system": "http://loinc.org",
                                "concept": [
                                  {
                                    "code": "96777-8",
                                    "display": "Accountable health communities (AHC) health-related social needs screening tool"
                                  }
                                ]
                              }
                            ]
                          }
                        }
                      ]
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request - Missing required fields or invalid UUID format", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "Missing UUIDs", summary = "Request missing required UUIDs", value = """
                            {
                              "error": "BAD_REQUEST",
                              "message": "UUIDs parameter is required and cannot be empty",
                              "timestamp": "2025-01-28T10:30:00Z"
                            }
                            """),
                    @ExampleObject(name = "Invalid UUID Format", summary = "Request with malformed UUID", value = """
                            {
                              "error": "BAD_REQUEST",
                              "message": "Invalid UUID format: 'invalid-uuid-format'",
                              "timestamp": "2025-01-28T10:30:00Z"
                            }
                            """)
            })),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Unauthorized", summary = "Missing or invalid authentication", value = """
                    {
                      "error": "UNAUTHORIZED",
                      "message": "Authentication required",
                      "timestamp": "2025-01-28T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Forbidden", summary = "Insufficient permissions", value = """
                    {
                      "error": "FORBIDDEN",
                      "message": "Insufficient permissions to access this resource",
                      "timestamp": "2025-01-28T10:30:00Z"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Internal server error - Unexpected system error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Internal Server Error", summary = "Unexpected system error", value = """
                    {
                      "error": "INTERNAL_SERVER_ERROR",
                      "message": "An unexpected error occurred while processing the request",
                      "timestamp": "2025-01-28T10:30:00Z"
                    }
                    """)))
    })
    @PreAuthorize("hasAuthority('SCOPE_ccmt:api')")
    public ResponseEntity<JsonResponse> searchResponses(
            @Valid @org.springframework.web.bind.annotation.RequestBody MasterUuidRequest request) {
        log.debug("Received request for operation: {} with priority: {}",
                request.getOperation(), request.getPriority());

        log.info("Processing search request for {} UUIDs",
                request.getParameters() != null && request.getParameters().getUuids() != null
                        ? request.getParameters().getUuids().size()
                        : 0);

        JsonResponse response = responseService.getResponses(
                request.getParameters().getUuids());

        // Enhance response with additional fields
        response.setResponseId(UUID.randomUUID().toString());
        response.setRequestId(UUID.randomUUID().toString());
        response.setTimestamp(ZonedDateTime.now());
        response.setStatus("SUCCESS");

        log.info("Successfully processed search request. Found {} responses",
                response.getResponses() != null ? response.getResponses().size() : 0);

        return ResponseEntity.ok(response);
    }
}