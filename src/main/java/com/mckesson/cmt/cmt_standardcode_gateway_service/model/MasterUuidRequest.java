package com.mckesson.cmt.cmt_standardcode_gateway_service.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MasterUuidRequest {
    @NotBlank(message = "Operation cannot be empty")
    private String operation;

    @Valid
    @NotNull(message = "Parameters cannot be null")
    private UuidParameters parameters;

    private String priority;
}