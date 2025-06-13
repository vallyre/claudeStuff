package com.mckesson.cmt.cmt_standardcode_gateway_service.contract;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class StandardCodesMasterResponse {
    private String responseId;
    private String requestId;
    private ZonedDateTime responseTime;
    private String status;
    private List<JsonNode> data;
    private ErrorDetails error;


    public static class ErrorDetails {
        private String code;
        private String message;
        private String details;

        // Constructors
        public ErrorDetails() {
        }

        public ErrorDetails(String code, String message, String details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        // Getters and setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }

}
