package com.mckesson.cmt.cmt_standardcode_gateway_service.contract;

import java.util.ArrayList;
import java.util.List;


public class BaseResponse {
    private static final String CREATED = "Created";

    private boolean isSuccess = false;
    private String message;
    private List<Error> errors = new ArrayList<>();

    public BaseResponse(boolean isSuccess2, String message2, List<Error> errors2) {
        //TODO Auto-generated constructor stub
    }

    public BaseResponse() {
        //TODO Auto-generated constructor stub
    }

    public static BaseResponse createErrorResponse(List<Error> errors, String message) {
        BaseResponse response = new BaseResponse();
        response.setErrors(errors);
        response.setMessage(message);
        return response;
    }

    public static <T extends BaseResponse> T createErrorResponse(Class<T> responseType, List<Error> errors,
            String message) {
        try {
            T response = responseType.getDeclaredConstructor().newInstance();
            response.setErrors(errors);
            response.setMessage(message);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create error response for " + responseType.getSimpleName(), e);
        }
    }

    public static BaseResponse createSuccessResponse(String message) {
        BaseResponse response = new BaseResponse();
        response.setMessage(message);
        response.setIsSuccess(true);
        return response;
    }

    public BaseResponse addErrorMessage(String errorMessage) {
        if (this.errors == null) {
            this.errors = new ArrayList<>(); // Ensure list is initialized
        }
        this.errors.add(new Error(errorMessage)); // Add new Error object
        this.isSuccess = false; // Mark as unsuccessful
        return this; // Return this for method chaining
    }

    /**
     * remove the following once lombok is fixed
     */

    public boolean isIsSuccess() {
        return this.isSuccess;
    }

    public boolean getIsSuccess() {
        return this.isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Error> getErrors() {
        return this.errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

}
