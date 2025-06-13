package com.mckesson.cmt.cmt_standardcode_gateway_service.services;

import com.mckesson.cmt.cmt_standardcode_gateway_service.client.HLIRestAPIClient;
import com.mckesson.cmt.cmt_standardcode_gateway_service.component.BatchProcessor;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMemberHLIRes;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMemberResDTO;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMembersReqDTO;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.HliApiConfig;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidBatchProcessLog;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidHliApiResponse;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.HliApiConfigRepository;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.OidBatchProcessLogRepository;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.OidHliApiResponseRepository;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.OidMasterRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OidProcessingService {

    private static final Logger log = LoggerFactory.getLogger(OidProcessingService.class);

    @Autowired
    private OidMasterRepository oidMasterRepository;

    @Autowired
    private OidHliApiResponseRepository oidHliApiResponseRepository;

    @Autowired
    private OidBatchProcessLogRepository oidBatchProcessLogRepository;

    @Autowired
    private HliApiConfigRepository hliApiConfigRepository;

    @Autowired
    private HLIRestAPIClient hliRestApiClient;

    @Autowired
    private BatchProcessor batchProcessor;

    @Value("${hli.api.batch-size:50}")
    private int batchSize;

    @Value("${hli.api.delay-ms:500}")
    private int delayMs;

    /**
     * Process a list of OIDs
     * 
     * @param oids The list of OIDs to process
     * @return A GroupMemberResDTO containing the processed OIDs
     */
    @Transactional
    public GroupMemberResDTO processOids(List<String> oids) {
        log.info("Processing {} OIDs", oids.size());

        // Find OidMaster records for the requested OIDs
        List<OidMaster> oidMasters = oidMasterRepository.findByOidIn(oids);

        if (oidMasters.isEmpty()) {
            log.warn("No OidMaster records found for the requested OIDs");
            GroupMemberResDTO result = new GroupMemberResDTO();
            result.setOids(new ArrayList<>());
            return result;
        }

        // Find OIDs that need processing (no current HLI response)
        List<OidMaster> oidsToProcess = findOidsNeedingProcessing(oidMasters);

        if (!oidsToProcess.isEmpty()) {
            // Process OIDs that need processing
            processPendingOids(oidsToProcess);
        }

        // Return the successfully processed OIDs
        GroupMemberResDTO result = new GroupMemberResDTO();
        result.setOids(oidMasters.stream().map(OidMaster::getOid).collect(Collectors.toList()));
        return result;
    }

    /**
     * Process OIDs with a specific request payload
     * 
     * @param request The request containing parameters for processing
     */
    @Transactional
    public void processOidsWithRequest(GroupMembersReqDTO request) {
        log.info("Processing OIDs with request ID: {}", request.getId());

        List<String> oids = request.getOids();
        if (oids == null || oids.isEmpty()) {
            log.warn("No OIDs provided in the request");
            return;
        }

        // Find OidMaster records for the requested OIDs
        List<OidMaster> oidMasters = oidMasterRepository.findByOidIn(oids);

        if (oidMasters.isEmpty()) {
            log.warn("No OidMaster records found for the requested OIDs");
            return;
        }

        // Find OIDs that need processing (no current HLI response)
        List<OidMaster> oidsToProcess = findOidsNeedingProcessing(oidMasters);

        if (!oidsToProcess.isEmpty()) {
            // Create batch process log
            OidBatchProcessLog batchLog = createBatchProcessLog(oidsToProcess);

            try {
                // Process each OID with the request
                processOidsWithRequestAndBatchLog(oidsToProcess, request, batchLog);

                // Update batch log with success
                updateBatchLogSuccess(batchLog, oidsToProcess.size());
            } catch (Exception e) {
                log.error("Error processing OIDs with request", e);
                // Update batch log with failure
                updateBatchLogFailure(batchLog, e.getMessage());
                throw e;
            }
        } else {
            log.info("No OIDs need processing");
        }
    }

    /**
     * Process all pending OIDs that need updating
     */
    @Transactional
    public void processAllPendingOids() {
        log.info("Processing all pending OIDs");

        // Find all active OIDs
        List<OidMaster> allActiveOids = oidMasterRepository.findByIsActiveTrue();

        // Find OIDs that need processing (no current HLI response)
        List<OidMaster> oidsToProcess = findOidsNeedingProcessing(allActiveOids);

        if (!oidsToProcess.isEmpty()) {
            // Create batch process log
            OidBatchProcessLog batchLog = createBatchProcessLog(oidsToProcess);

            try {
                // Process OIDs in batches
                processPendingOidsWithBatchLog(oidsToProcess, batchLog);

                // Update batch log with success
                updateBatchLogSuccess(batchLog, oidsToProcess.size());
            } catch (Exception e) {
                log.error("Error processing all pending OIDs", e);
                // Update batch log with failure
                updateBatchLogFailure(batchLog, e.getMessage());
                throw e;
            }
        } else {
            log.info("No OIDs need processing");
        }
    }

    /**
     * Find OIDs that need processing (those without a current HLI response)
     * 
     * @param oidMasters The list of OidMaster entities to check
     * @return A list of OidMaster entities that need processing
     */
    private List<OidMaster> findOidsNeedingProcessing(List<OidMaster> oidMasters) {
        List<OidMaster> oidsToProcess = new ArrayList<>();

        for (OidMaster oidMaster : oidMasters) {
            // Check if there's a current HLI response for this OID
            Optional<OidHliApiResponse> currentResponse = oidHliApiResponseRepository
                    .findByOidMasterAndIsCurrentTrue(oidMaster);

            if (currentResponse.isEmpty()) {
                oidsToProcess.add(oidMaster);
            }
        }

        log.info("Found {} OIDs that need processing", oidsToProcess.size());
        return oidsToProcess;
    }

    /**
     * Process pending OIDs
     * 
     * @param oidsToProcess The list of OidMaster entities to process
     */
    private void processPendingOids(List<OidMaster> oidsToProcess) {
        log.info("Processing {} pending OIDs", oidsToProcess.size());

        // Create batch process log
        OidBatchProcessLog batchLog = createBatchProcessLog(oidsToProcess);

        try {
            // Process OIDs in batches
            processPendingOidsWithBatchLog(oidsToProcess, batchLog);

            // Update batch log with success
            updateBatchLogSuccess(batchLog, oidsToProcess.size());
        } catch (Exception e) {
            log.error("Error processing pending OIDs", e);
            // Update batch log with failure
            updateBatchLogFailure(batchLog, e.getMessage());
            throw e;
        }
    }

    /**
     * Process pending OIDs with batch log
     * 
     * @param oidsToProcess The list of OidMaster entities to process
     * @param batchLog      The batch process log
     */
    private void processPendingOidsWithBatchLog(List<OidMaster> oidsToProcess, OidBatchProcessLog batchLog) {
        log.info("Processing {} pending OIDs with batch log", oidsToProcess.size());

        // Group OIDs by service method name
        oidsToProcess.stream()
                .collect(Collectors.groupingBy(
                        oidMaster -> oidMaster.getServiceMethodName() != null ? oidMaster.getServiceMethodName()
                                : "defaultProcessOid"))
                .forEach((methodName, oids) -> {
                    try {
                        // Invoke the specified service method for each group
                        invokeDynamicServiceMethod(methodName, oids, batchLog);
                    } catch (Exception e) {
                        log.error("Error invoking service method: {}", methodName, e);
                        throw new RuntimeException("Error invoking service method: " + methodName, e);
                    }
                });
    }

    /**
     * Process OIDs with request and batch log
     * 
     * @param oidsToProcess The list of OidMaster entities to process
     * @param request       The request containing parameters for processing
     * @param batchLog      The batch process log
     */
    private void processOidsWithRequestAndBatchLog(
            List<OidMaster> oidsToProcess,
            GroupMembersReqDTO request,
            OidBatchProcessLog batchLog) {

        log.info("Processing {} OIDs with request and batch log", oidsToProcess.size());

        // Use batch processor to process OIDs in batches
        batchProcessor.processBatches(
                oidsToProcess,
                batchSize,
                delayMs,
                oidMaster -> processOidWithRequest(oidMaster, request, batchLog));
    }

    /**
     * Process a single OID with request
     * 
     * @param oidMaster The OidMaster entity to process
     * @param request   The request containing parameters for processing
     * @param batchLog  The batch process log
     * @return A Mono<Void> representing the completion of the processing
     */
    private Mono<Void> processOidWithRequest(
            OidMaster oidMaster,
            GroupMembersReqDTO request,
            OidBatchProcessLog batchLog) {

        long startTime = System.currentTimeMillis();

        // Create a copy of the request with this specific OID
        GroupMembersReqDTO oidRequest = copyRequestWithOid(request, oidMaster.getOid());

        return hliRestApiClient.sendHliApiRequest(oidRequest)
                .doOnSuccess(response -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    // Save response to database
                    saveHliApiResponse(oidMaster, response, responseTime, 200);
                    // Update batch log
                    incrementBatchLogSuccess(batchLog);
                })
                .doOnError(error -> {
                    log.error("Error processing OID: {}", oidMaster.getOid(), error);
                    // Handle error response
                    long responseTime = System.currentTimeMillis() - startTime;
                    saveErrorResponse(oidMaster, error.getMessage(), responseTime);
                    // Update batch log
                    incrementBatchLogFailure(batchLog);
                })
                .then();
    }

    /**
     * Invoke a dynamic service method based on the method name stored in OidMaster
     * 
     * @param methodName The name of the method to invoke
     * @param oids       The list of OidMaster entities to process
     * @param batchLog   The batch process log
     * @throws Exception If there is an error invoking the method
     */
    private void invokeDynamicServiceMethod(String methodName, List<OidMaster> oids, OidBatchProcessLog batchLog)
            throws Exception {

        // If method name is "defaultProcessOid" or null, use the default method
        if (methodName == null || "defaultProcessOid".equals(methodName)) {
            log.info("Using default process method for {} OIDs", oids.size());
            defaultProcessOid(oids, batchLog);
            return;
        }

        // Try to find and invoke the specified method
        try {
            Method method = this.getClass().getDeclaredMethod(methodName, List.class, OidBatchProcessLog.class);
            log.info("Invoking method {} for {} OIDs", methodName, oids.size());
            method.invoke(this, oids, batchLog);
        } catch (NoSuchMethodException e) {
            log.warn("Method {} not found, using default process method", methodName);
            defaultProcessOid(oids, batchLog);
        }
    }

    /**
     * Default method for processing OIDs
     * 
     * @param oids     The list of OidMaster entities to process
     * @param batchLog The batch process log
     */
    private void defaultProcessOid(List<OidMaster> oids, OidBatchProcessLog batchLog) {
        log.info("Default processing for {} OIDs", oids.size());

        // Create a simple request
        GroupMembersReqDTO request = new GroupMembersReqDTO();
        request.setId(UUID.randomUUID().toString());

        // Use batch processor to process OIDs in batches
        batchProcessor.processBatches(
                oids,
                batchSize,
                delayMs,
                oidMaster -> processOidWithRequest(oidMaster, request, batchLog));
    }

    /**
     * Process OIDs for LOINC codes
     * Example of a specialized method that could be referenced in
     * oid_master.service_method_name
     * 
     * @param oids     The list of OidMaster entities to process
     * @param batchLog The batch process log
     */
    private void processLoincOid(List<OidMaster> oids, OidBatchProcessLog batchLog) {
        log.info("Processing {} LOINC OIDs", oids.size());

        // Create a specialized request for LOINC codes
        GroupMembersReqDTO request = new GroupMembersReqDTO();
        request.setId(UUID.randomUUID().toString());

        // Add LOINC-specific fields to the request
        List<String> fields = new ArrayList<>();
        fields.add("COMPONENT");
        fields.add("PROPERTY");
        fields.add("TIME_ASPCT");
        fields.add("SYSTEM");
        fields.add("SCALE_TYP");
        fields.add("METHOD_TYP");
        request.setFields(fields);

        // Use batch processor to process OIDs in batches
        batchProcessor.processBatches(
                oids,
                batchSize,
                delayMs,
                oidMaster -> processOidWithRequest(oidMaster, request, batchLog));
    }

    /**
     * Process OIDs for SNOMED CT codes
     * Example of a specialized method that could be referenced in
     * oid_master.service_method_name
     * 
     * @param oids     The list of OidMaster entities to process
     * @param batchLog The batch process log
     */
    private void processSnomedOid(List<OidMaster> oids, OidBatchProcessLog batchLog) {
        log.info("Processing {} SNOMED CT OIDs", oids.size());

        // Create a specialized request for SNOMED codes
        GroupMembersReqDTO request = new GroupMembersReqDTO();
        request.setId(UUID.randomUUID().toString());

        // Add SNOMED-specific fields to the request
        List<String> fields = new ArrayList<>();
        fields.add("FSN");
        fields.add("CONCEPTID");
        fields.add("SEMANTIC_TAG");
        request.setFields(fields);

        // Use batch processor to process OIDs in batches
        batchProcessor.processBatches(
                oids,
                batchSize,
                delayMs,
                oidMaster -> processOidWithRequest(oidMaster, request, batchLog));
    }

    /**
     * Create a copy of the request with a specific OID
     * 
     * @param request The original request
     * @param oid     The OID to set in the copy
     * @return A new GroupMembersReqDTO with the specific OID
     */
    private GroupMembersReqDTO copyRequestWithOid(GroupMembersReqDTO request, String oid) {
        GroupMembersReqDTO copy = new GroupMembersReqDTO();

        // Copy all fields from the original request
        copy.setId(request.getId());
        copy.setOids(request.getOids());
        copy.setUrl(request.getUrl());
        copy.setRevisionDate(request.getRevisionDate());
        copy.setCount(request.getCount());
        copy.setNextCursor(request.getNextCursor());
        copy.setFields(request.getFields());
        copy.setEffectiveDate(request.getEffectiveDate());
        copy.setIncludeInvalid(request.getIncludeInvalid());
        copy.setIncludeRetired(request.getIncludeRetired());

        // Set single OID instead of list of IDs
        /**List<String> singleOid = new ArrayList<>();
        singleOid.add(oid);
        copy.setOids(singleOid);**/

        return copy;
    }

    /**
     * Save HLI API response to the database
     * 
     * @param oidMaster      The OidMaster entity
     * @param response       The HLI API response
     * @param responseTimeMs The response time in milliseconds
     * @param httpStatusCode The HTTP status code
     */
    private void saveHliApiResponse(
            OidMaster oidMaster,
            GroupMemberHLIRes response,
            long responseTimeMs,
            int httpStatusCode) {

        log.info("Saving HLI API response for OID: {}", oidMaster.getOid());

        // Mark all existing responses for this OID as not current
        List<OidHliApiResponse> existingResponses = oidHliApiResponseRepository.findByOidMaster(oidMaster);

        for (OidHliApiResponse existingResponse : existingResponses) {
            existingResponse.setIsCurrent(false);
            oidHliApiResponseRepository.save(existingResponse);
        }

        // Calculate new version number
        int newVersion = 1;
        if (!existingResponses.isEmpty()) {
            Optional<OidHliApiResponse> latestResponse = oidHliApiResponseRepository
                    .findByOidMasterOrderByVersionDesc(oidMaster)
                    .stream()
                    .findFirst();

            if (latestResponse.isPresent()) {
                newVersion = latestResponse.get().getVersion() + 1;
            }
        }

        // Create and save new response
        OidHliApiResponse newResponse = new OidHliApiResponse();
        newResponse.setOidMaster(oidMaster);
        newResponse.setApiResponse(convertResponseToJson(response));
        newResponse.setResponseTimeMs((int) responseTimeMs);
        newResponse.setHttpStatusCode(httpStatusCode);
        newResponse.setCreatedAt(LocalDateTime.now());
        newResponse.setVersion(newVersion);
        newResponse.setIsCurrent(true);

        oidHliApiResponseRepository.save(newResponse);

        // Update OidMaster last updated
        oidMaster.setLastModifiedDate(LocalDateTime.now());
        oidMasterRepository.save(oidMaster);
    }

    /**
     * Save error response to the database
     * 
     * @param oidMaster      The OidMaster entity
     * @param errorMessage   The error message
     * @param responseTimeMs The response time in milliseconds
     */
    private void saveErrorResponse(OidMaster oidMaster, String errorMessage, long responseTimeMs) {
        log.info("Saving error response for OID: {}", oidMaster.getOid());

        // Create error JSON response
        String errorJson = "{\"error\": \"" + errorMessage.replace("\"", "\\\"") + "\"}";

        // Calculate new version number
        List<OidHliApiResponse> existingResponses = oidHliApiResponseRepository.findByOidMaster(oidMaster);

        int newVersion = 1;
        if (!existingResponses.isEmpty()) {
            Optional<OidHliApiResponse> latestResponse = oidHliApiResponseRepository
                    .findByOidMasterOrderByVersionDesc(oidMaster)
                    .stream()
                    .findFirst();

            if (latestResponse.isPresent()) {
                newVersion = latestResponse.get().getVersion() + 1;
            }
        }

        // Create and save error response
        OidHliApiResponse errorResponse = new OidHliApiResponse();
        errorResponse.setOidMaster(oidMaster);
        errorResponse.setApiResponse(errorJson);
        errorResponse.setResponseTimeMs((int) responseTimeMs);
        errorResponse.setHttpStatusCode(500); // Internal server error
        errorResponse.setCreatedAt(LocalDateTime.now());
        errorResponse.setVersion(newVersion);
        errorResponse.setIsCurrent(false); // Error responses are not marked as current

        oidHliApiResponseRepository.save(errorResponse);
    }

    /**
     * Convert GroupMemberHLIRes to JSON string
     * 
     * @param response The GroupMemberHLIRes to convert
     * @return A JSON string representation of the response
     */
    private String convertResponseToJson(GroupMemberHLIRes response) {
        // In a real implementation, use a JSON library like Jackson or Gson
        // This is a simple implementation for demonstration purposes
        StringBuilder json = new StringBuilder();
        json.append("{");

        if (response.getResults() != null) {
            json.append("\"results\": [");
            for (int i = 0; i < response.getResults().size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append("{");
                json.append("\"id\": \"").append(response.getResults().get(i).getId()).append("\",");
                json.append("\"name\": \"").append(response.getResults().get(i).getName()).append("\",");
                json.append("\"code\": \"").append(response.getResults().get(i).getCode()).append("\",");
                json.append("\"codeSystemId\": \"").append(response.getResults().get(i).getCodeSystemId())
                        .append("\",");
                json.append("\"valid\": ").append(response.getResults().get(i).isValid());

                if (response.getResults().get(i).getProperties() != null) {
                    json.append(",\"properties\": [");
                    for (int j = 0; j < response.getResults().get(i).getProperties().size(); j++) {
                        if (j > 0) {
                            json.append(",");
                        }
                        json.append("{");
                        json.append("\"id\": \"").append(response.getResults().get(i).getProperties().get(j).getId())
                                .append("\",");
                        json.append("\"name\": \"")
                                .append(response.getResults().get(i).getProperties().get(j).getName()).append("\",");
                        json.append("\"value\": \"")
                                .append(response.getResults().get(i).getProperties().get(j).getValue()).append("\"");
                        json.append("}");
                    }
                    json.append("]");
                }

                json.append("}");
            }
            json.append("],");
        }

        if (response.getNextCursor() != null) {
            json.append("\"nextCursor\": \"").append(response.getNextCursor()).append("\"");
        } else {
            // Remove trailing comma if there's no nextCursor
            if (json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Create a batch process log
     * 
     * @param oidsToProcess The list of OidMaster entities to process
     * @return The created OidBatchProcessLog
     */
    private OidBatchProcessLog createBatchProcessLog(List<OidMaster> oidsToProcess) {
        log.info("Creating batch process log for {} OIDs", oidsToProcess.size());

        // Get first OID's HLI API config
        OidMaster firstOid = oidsToProcess.get(0);
        HliApiConfig hliApiConfig = firstOid.getHliApiConfig();

        // Create batch process log
        OidBatchProcessLog batchLog = new OidBatchProcessLog();
        batchLog.setBatchId(UUID.randomUUID().toString());
        batchLog.setHliApiConfig(hliApiConfig);
        batchLog.setBatchStartTime(LocalDateTime.now());
        batchLog.setTotalOids(oidsToProcess.size());
        batchLog.setSuccessfulOids(0);
        batchLog.setFailedOids(0);
        batchLog.setStatus("PROCESSING");

        return oidBatchProcessLogRepository.save(batchLog);
    }

    /**
     * Update batch log for successful completion
     * 
     * @param batchLog       The batch process log to update
     * @param totalProcessed The total number of OIDs processed
     */
    private void updateBatchLogSuccess(OidBatchProcessLog batchLog, int totalProcessed) {
        log.info("Updating batch log {} for successful completion", batchLog.getBatchId());

        batchLog.setBatchEndTime(LocalDateTime.now());
        batchLog.setStatus("COMPLETED");

        oidBatchProcessLogRepository.save(batchLog);
    }

    /**
     * Update batch log for failure
     * 
     * @param batchLog     The batch process log to update
     * @param errorMessage The error message
     */
    private void updateBatchLogFailure(OidBatchProcessLog batchLog, String errorMessage) {
        log.info("Updating batch log {} for failure", batchLog.getBatchId());

        batchLog.setBatchEndTime(LocalDateTime.now());
        batchLog.setStatus("FAILED");
        batchLog.setErrorMessage(errorMessage);

        oidBatchProcessLogRepository.save(batchLog);
    }

    /**
     * Increment the successful OIDs count in the batch log
     * 
     * @param batchLog The batch process log to update
     */
    private synchronized void incrementBatchLogSuccess(OidBatchProcessLog batchLog) {
        batchLog.setSuccessfulOids(batchLog.getSuccessfulOids() + 1);
        oidBatchProcessLogRepository.save(batchLog);
    }

    /**
     * Increment the failed OIDs count in the batch log
     * 
     * @param batchLog The batch process log to update
     */
    private synchronized void incrementBatchLogFailure(OidBatchProcessLog batchLog) {
        batchLog.setFailedOids(batchLog.getFailedOids() + 1);
        oidBatchProcessLogRepository.save(batchLog);
    }
}