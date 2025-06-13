package com.mckesson.cmt.cmt_standardcode_gateway_service.contract;

public class BaseRequest {
    private String requestId;

    // Default constructor
    public BaseRequest() {
    }

    // Constructor with all fields
    public BaseRequest(String requestId) {
        this.requestId = requestId;
    }

    // Getters and setters
    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
