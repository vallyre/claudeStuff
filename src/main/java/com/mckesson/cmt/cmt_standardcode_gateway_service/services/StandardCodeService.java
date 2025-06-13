package com.mckesson.cmt.cmt_standardcode_gateway_service.services;

import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.Error;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.StandardCodeRequest;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.StandardCodeResponse;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class StandardCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(StandardCodeService.class);
    
    public StandardCodeResponse getStandardCodes(StandardCodeRequest request) {
        logger.info("Processing standard codes request with ID: {}", request.getRequestId());
        
        try {
            // Validate the request
            if (request.getOperation() == null || !request.getOperation().equals("getStandardCodes")) {
                return createErrorResponse(request, "INVALID_OPERATION", "Operation must be 'getStandardCodes'");
            }
            
            if (request.getParameters() == null || request.getParameters().getOidRevisions() == null 
                    || request.getParameters().getOidRevisions().isEmpty()) {
                return createErrorResponse(request, "INVALID_PARAMETER", "OID-revision pairs are required");
            }
            
            // Process each OID-revision pair and generate the response
            List<StandardCodeResponse.StandardCodeData> dataList = new ArrayList<>();
            
            for (StandardCodeRequest.OidRevision oidRevision : request.getParameters().getOidRevisions()) {
                // Here you would typically fetch data from a database or another service
                // For this example, we'll generate mock data based on the OID
                StandardCodeResponse.StandardCodeData data = generateMockData(oidRevision);
                dataList.add(data);
            }
            
            // Create successful response
            StandardCodeResponse response = new StandardCodeResponse();
            response.setResponseId(UUID.randomUUID().toString());
            response.setRequestId(request.getRequestId());
            response.setTimestamp(ZonedDateTime.now());
            response.setStatus("success");
            response.setData(dataList);
            response.setIsSuccess(true);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing request: {}", e.getMessage(), e);
            return createErrorResponse(request, "INTERNAL_ERROR", "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    private StandardCodeResponse createErrorResponse(StandardCodeRequest request, String errorCode, String errorMessage) {
        StandardCodeResponse response = new StandardCodeResponse();
        response.setResponseId(UUID.randomUUID().toString());
        response.setRequestId(request.getRequestId());
        response.setTimestamp(ZonedDateTime.now());
        response.setStatus("error");
        
        StandardCodeResponse.ErrorDetails errorDetails = new StandardCodeResponse.ErrorDetails();
        errorDetails.setCode(errorCode);
        errorDetails.setMessage(errorMessage);
        response.setError(errorDetails);
        
        // Also populate the base response error fields
        List<Error> errors = new ArrayList<>();
        errors.add(new Error(errorMessage));
        response.setErrors(errors);
        response.setMessage(errorMessage);
        response.setIsSuccess(false);
        
        return response;
    }
    
    private StandardCodeResponse.StandardCodeData generateMockData(StandardCodeRequest.OidRevision oidRevision) {
        StandardCodeResponse.StandardCodeData data = new StandardCodeResponse.StandardCodeData();
        data.setOId(oidRevision.getOId());
        
        // Use the provided revision or default to current date if empty
        String revision = oidRevision.getRevision();
        if (revision == null || revision.isEmpty()) {
            revision = "20250428"; // Today's date in YYYYMMDD format
        }
        data.setRevision(revision);
        
        // Generate mock results based on the OID
        List<StandardCodeResponse.StandardCodeResult> results = new ArrayList<>();
        
        // First result always indicates whether it's a questionnaire
        StandardCodeResponse.StandardCodeResult typeIndicator = new StandardCodeResponse.StandardCodeResult();
        
        // Decide if this should be a questionnaire or flat list based on the OID
        boolean isQuestionnaire = oidRevision.getOId().endsWith("240");
        typeIndicator.setIsQuestionnaire(isQuestionnaire);
        results.add(typeIndicator);
        
        // Add more mock results
        if (isQuestionnaire) {
            // Add a question with options
            StandardCodeResponse.StandardCodeResult question = createMockQuestion(oidRevision.getOId());
            results.add(question);
        } else {
            // Add a flat list item
            StandardCodeResponse.StandardCodeResult item = createMockFlatListItem(oidRevision.getOId());
            results.add(item);
        }
        
        data.setResults(results);
        data.setNextCursor(null); // No pagination for this example
        
        return data;
    }
    
    private StandardCodeResponse.StandardCodeResult createMockQuestion(String oid) {
        StandardCodeResponse.StandardCodeResult question = new StandardCodeResponse.StandardCodeResult();
        question.setId(UUID.randomUUID().toString());
        question.setName("Example question for " + oid);
        question.setCode("99802-" + (Math.abs(oid.hashCode()) % 10));
        question.setDefinition(null);
        question.setCodeSystemId("loinc");
        
        // Add some mock options
        List<StandardCodeResponse.Option> options = new ArrayList<>();
        
        StandardCodeResponse.Option option1 = new StandardCodeResponse.Option();
        option1.setId(UUID.randomUUID().toString());
        option1.setName("Option A");
        option1.setCode("LA33217-3");
        options.add(option1);
        
        StandardCodeResponse.Option option2 = new StandardCodeResponse.Option();
        option2.setId(UUID.randomUUID().toString());
        option2.setName("Option B");
        option2.setCode("LA33218-1");
        options.add(option2);
        
        question.setOptions(options);
        
        return question;
    }
    
    private StandardCodeResponse.StandardCodeResult createMockFlatListItem(String oid) {
        StandardCodeResponse.StandardCodeResult item = new StandardCodeResponse.StandardCodeResult();
        item.setId(UUID.randomUUID().toString());
        item.setName("Standard code for " + oid);
        item.setCode("99802-" + (Math.abs(oid.hashCode()) % 10));
        item.setDefinition(null);
        item.setCodeSystemId("loinc");
        item.setOptions(new ArrayList<>()); // Empty options for flat list
        
        return item;
    }


    public void processOid(OidMaster oidMaster) {
    String methodName = oidMaster.getServiceMethodName();
    if (methodName != null && !methodName.isEmpty()) {
        try {
            Method method = this.getClass().getMethod(methodName, OidMaster.class);
            method.invoke(this, oidMaster);
        } catch (Exception e) {
            logger.error("Error invoking service method: {}", methodName, e);
            // Handle the exception appropriately
        }
    } else {
        // Use default processing method
        defaultProcessOid(oidMaster);
    }
}

    private void defaultProcessOid(OidMaster oidMaster) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'defaultProcessOid'");
    }
}