package com.mckesson.cmt.cmt_standardcode_gateway_service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for batch upload results
 */
public class BatchUploadResult {

    private int totalRecords;
    private int createdCount;
    private int updatedCount;
    private int skippedCount;
    private int failedCount;
    private List<String> errorMessages = new ArrayList<>();

    public BatchUploadResult() {
        // Default constructor
    }

    public void incrementCreated() {
        this.createdCount++;
        this.totalRecords++;
    }

    public void incrementUpdated() {
        this.updatedCount++;
        this.totalRecords++;
    }

    public void incrementSkipped() {
        this.skippedCount++;
        this.totalRecords++;
    }

    public void incrementFailed() {
        this.failedCount++;
        this.totalRecords++;
    }

    public void addErrorMessage(String errorMessage) {
        errorMessages.add(errorMessage);
    }

    // Getters and setters
    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    @Override
    public String toString() {
        return "BatchUploadResult{" +
                "totalRecords=" + totalRecords +
                ", createdCount=" + createdCount +
                ", updatedCount=" + updatedCount +
                ", skippedCount=" + skippedCount +
                ", failedCount=" + failedCount +
                ", errorMessages.size=" + errorMessages.size() +
                '}';
    }
}