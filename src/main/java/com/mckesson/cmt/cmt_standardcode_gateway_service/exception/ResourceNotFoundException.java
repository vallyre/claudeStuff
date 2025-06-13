package com.mckesson.cmt.cmt_standardcode_gateway_service.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}