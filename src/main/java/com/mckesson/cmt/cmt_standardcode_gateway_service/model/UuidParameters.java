package com.mckesson.cmt.cmt_standardcode_gateway_service.model;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Data
public class UuidParameters {
    @NotEmpty(message = "Master UUIDs cannot be empty")
    private List<UUID> uuids;
}