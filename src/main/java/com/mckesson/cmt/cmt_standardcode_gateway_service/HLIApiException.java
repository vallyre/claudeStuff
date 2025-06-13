package com.mckesson.cmt.cmt_standardcode_gateway_service;


public class HLIApiException extends RuntimeException {

    public HLIApiException(String message) {
        super(message);
    }

    public HLIApiException(String message, Throwable cause) {
        super(message, cause);
    }
}