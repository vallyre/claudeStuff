package com.mckesson.cmt.cmt_standardcode_gateway_service.services;

import com.mckesson.cmt.cmt_standardcode_gateway_service.dto.BatchUploadResult;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.HliApiConfig;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.HliApiConfigRepository;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.OidMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service for directly processing CSV data without using OpenCSV
 */
@Service
public class OidBatchServiceDebug {

    private static final Logger log = LoggerFactory.getLogger(OidBatchServiceDebug.class);

    private final OidMasterRepository oidMasterRepository;
    private final HliApiConfigRepository hliApiConfigRepository;

    @Autowired
    public OidBatchServiceDebug(
            OidMasterRepository oidMasterRepository,
            HliApiConfigRepository hliApiConfigRepository) {
        this.oidMasterRepository = oidMasterRepository;
        this.hliApiConfigRepository = hliApiConfigRepository;
    }

    /**
     * Process a CSV file for OidMaster batch upload
     * This method skips OpenCSV and directly processes the CSV data
     * 
     * @param file           The CSV file to process
     * @param hliApiConfigId The HLI API Config ID to associate with the records
     *                       (optional)
     * @param batchSize      The batch size for processing
     * @param username       The username performing the upload
     * @return Result of the batch upload operation
     */
    @Transactional
    public BatchUploadResult processCsvUpload(
            MultipartFile file,
            Long hliApiConfigId,
            int batchSize,
            String username) throws Exception {

        log.info("Processing OID CSV upload with direct processing, batch size: {}", batchSize);

        // Get HLI API Config if provided
        HliApiConfig hliApiConfig = null;
        if (hliApiConfigId != null) {
            hliApiConfig = hliApiConfigRepository.findById(hliApiConfigId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("HLI API Config not found with ID: " + hliApiConfigId));
        } else {
            // If no config ID provided, get a default one if available
            List<HliApiConfig> configs = hliApiConfigRepository.findAll();
            if (!configs.isEmpty()) {
                hliApiConfig = configs.get(0);
                log.info("Using default HLI API Config with ID: {}", hliApiConfig.getId());
            }
        }

        // Create a result object to track statistics
        BatchUploadResult result = new BatchUploadResult();

        // Directly read and process the CSV file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read the header line to get column indexes
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            // Log the header line for debugging
            log.info("CSV header line: {}", headerLine);

            // Parse header to get column indexes
            Map<String, Integer> columnIndexes = parseHeader(headerLine);
            log.info("Parsed column indexes: {}", columnIndexes);

            // Process each data line
            String line;
            int lineNumber = 1; // Start with 1 for the header line
            List<OidMaster> entitiesToSave = new ArrayList<>();
            int batchCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    // Skip empty lines
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    // Parse the line into a map of column name to value
                    Map<String, String> record = parseLine(line, columnIndexes);
                    log.debug("Parsed record: {}", record);

                    // Create or update an OidMaster entity
                    OidMaster entity = createEntityFromRecord(record, hliApiConfig, username);
                    if (entity != null) {
                        entitiesToSave.add(entity);
                        result.incrementCreated();
                        batchCount++;
                    }

                    // Process in batches
                    if (batchCount >= batchSize) {
                        oidMasterRepository.saveAll(entitiesToSave);
                        log.info("Saved batch of {} records", entitiesToSave.size());
                        entitiesToSave.clear();
                        batchCount = 0;
                    }
                } catch (Exception e) {
                    log.error("Error processing line {}: {}", lineNumber, line, e);
                    result.incrementFailed();
                    result.addErrorMessage("Row " + lineNumber + ": " + e.getMessage());
                }
            }

            // Save any remaining records
            if (!entitiesToSave.isEmpty()) {
                oidMasterRepository.saveAll(entitiesToSave);
                log.info("Saved final batch of {} records", entitiesToSave.size());
            }
        }

        log.info("Completed CSV processing: {}", result);
        return result;
    }

    /**
     * Parse the CSV header line to get column indexes
     */
    private Map<String, Integer> parseHeader(String headerLine) {
        Map<String, Integer> columnIndexes = new HashMap<>();
        String[] headers = headerLine.split(",", -1);

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            columnIndexes.put(header, i);

            // Also add uppercase and lowercase versions for case-insensitive matching
            columnIndexes.put(header.toUpperCase(), i);
            columnIndexes.put(header.toLowerCase(), i);
        }

        return columnIndexes;
    }

    /**
     * Parse a CSV line into a map of column name to value
     */
    private Map<String, String> parseLine(String line, Map<String, Integer> columnIndexes) {
        Map<String, String> record = new HashMap<>();
        String[] values = line.split(",", -1);

        // Map known column names to values
        mapIfPresent(record, "Code Group Content Set", values, columnIndexes);
        mapIfPresent(record, "Code Group Content Set Version", values, columnIndexes);
        mapIfPresent(record, "Code Sub-type", values, columnIndexes);
        mapIfPresent(record, "OID", values, columnIndexes);
        mapIfPresent(record, "Code Group Name", values, columnIndexes);
        mapIfPresent(record, "Code Group Revision Name", values, columnIndexes);
        mapIfPresent(record, "Member Code System", values, columnIndexes);
        mapIfPresent(record, "Description", values, columnIndexes);
        mapIfPresent(record, "Revision start", values, columnIndexes);
        mapIfPresent(record, "Revision end", values, columnIndexes);

        return record;
    }

    /**
     * Map a column value if the column exists
     */
    private void mapIfPresent(Map<String, String> record, String columnName, String[] values,
            Map<String, Integer> columnIndexes) {
        Integer index = columnIndexes.get(columnName);
        if (index != null && index < values.length) {
            record.put(columnName, values[index].trim());
        }
    }

    /**
     * Create a new OidMaster entity from a parsed record
     */
    private OidMaster createEntityFromRecord(Map<String, String> record, HliApiConfig hliApiConfig, String username) {
        // Check required fields
        String oid = record.get("OID");
        String codeGroupContentSet = record.get("Code Group Content Set");
        String codeGroupName = record.get("Code Group Name");

        if (oid == null || oid.isEmpty()) {
            throw new IllegalArgumentException("OID is required");
        }
        if (codeGroupContentSet == null || codeGroupContentSet.isEmpty()) {
            throw new IllegalArgumentException("Code Group Content Set is required");
        }
        if (codeGroupName == null || codeGroupName.isEmpty()) {
            throw new IllegalArgumentException("Code Group Name is required");
        }

        OidMaster entity = new OidMaster();

        // Set required fields
        entity.setOid(oid);
        entity.setCodeGroupContentSet(codeGroupContentSet);

        // Set content set version (may be a float in the CSV)
        String codeGroupContentSetVersion = record.get("Code Group Content Set Version");
        if (codeGroupContentSetVersion != null && !codeGroupContentSetVersion.isEmpty()) {
            entity.setCodeGroupContentSetVersion(codeGroupContentSetVersion);
        } else {
            entity.setCodeGroupContentSetVersion("1.0"); // Default
        }

        // Set code from the description field or extract from OID if needed
        String description = record.get("Description");
        if (description != null && !description.isEmpty()) {
            entity.setCode(description);
        } else {
            entity.setCode(extractCodeFromOid(oid));
        }

        entity.setCodeGroupName(codeGroupName);
        entity.setHliApiConfig(hliApiConfig);

        // Set optional fields if provided
        String codeSubType = record.get("Code Sub-type");
        if (codeSubType != null && !codeSubType.isEmpty()) {
            entity.setCodeSubType(codeSubType);
        }

        String codeGroupRevisionName = record.get("Code Group Revision Name");
        if (codeGroupRevisionName != null && !codeGroupRevisionName.isEmpty()) {
            entity.setCodeGroupRevisionName(codeGroupRevisionName);
        }

        // Set FHIR identifier and HL7 URI based on OID
        String memberCodeSystem = record.get("Member Code System");
        setupIdentifiersFromOid(entity, oid, memberCodeSystem);

        // Parse revision start date
        String revisionStart = record.get("Revision start");
        if (revisionStart != null && !revisionStart.isEmpty()) {
            try {
                entity.setRevisionStart(LocalDateTime.parse(revisionStart + "T00:00:00"));
            } catch (DateTimeParseException e) {
                log.warn("Unable to parse revision start date: {}", revisionStart);
            }
        }

        // Set status to active by default
        entity.setIsActive(true);
        entity.setStatus("ACTIVE");

        // Determine appropriate service method name based on code system
        entity.setServiceMethodName(determineServiceMethodFromOid(oid, memberCodeSystem));

        // Set audit fields
        entity.setCreatedBy(username);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedBy(username);
        entity.setLastModifiedDate(LocalDateTime.now());

        return entity;
    }

    /**
     * Extract code from OID string (e.g., "2.16.840.1.113883.6.1:12345" → "12345")
     */
    private String extractCodeFromOid(String oid) {
        if (oid == null) {
            return null;
        }

        // Check if the OID contains a code portion after a separator
        int separatorIndex = oid.lastIndexOf(':');
        if (separatorIndex > 0 && separatorIndex < oid.length() - 1) {
            return oid.substring(separatorIndex + 1);
        }

        // Return the full OID if no code portion is found
        return oid;
    }

    /**
     * Set FHIR identifier and HL7 URI based on OID and member code system
     */
    private void setupIdentifiersFromOid(OidMaster entity, String oid, String memberCodeSystem) {
        if (oid == null) {
            return;
        }

        // Extract the OID root (before the colon if it exists)
        String oidRoot = oid;
        int colonIndex = oid.indexOf(':');
        if (colonIndex > 0) {
            oidRoot = oid.substring(0, colonIndex);
        }

        // Set HL7 URI based on the OID root
        entity.setHl7Uri("urn:oid:" + oidRoot);

        // Determine FHIR identifier based on the OID or member code system
        if (oidRoot.equals("2.16.840.1.113883.6.1") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("loinc"))) {
            entity.setFhirIdentifier("http://loinc.org");
        } else if (oidRoot.equals("2.16.840.1.113883.6.96") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("snomed"))) {
            entity.setFhirIdentifier("http://snomed.info/sct");
        } else if (oidRoot.equals("2.16.840.1.113883.6.88") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("rxnorm"))) {
            entity.setFhirIdentifier("http://www.nlm.nih.gov/research/umls/rxnorm");
        } else if (oidRoot.equals("2.16.840.1.113883.6.90") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("icd-10"))) {
            entity.setFhirIdentifier("http://hl7.org/fhir/sid/icd-10-cm");
        } else if (oidRoot.equals("2.16.840.1.113883.6.103") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("icd-9"))) {
            entity.setFhirIdentifier("http://hl7.org/fhir/sid/icd-9-cm");
        } else if (oidRoot.equals("2.16.840.1.113883.6.12") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("cpt"))) {
            entity.setFhirIdentifier("http://www.ama-assn.org/go/cpt");
        } else if (oidRoot.equals("2.16.840.1.113883.6.285") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("hcpcs"))) {
            entity.setFhirIdentifier("http://www.cms.gov/Medicare/Coding/HCPCSReleaseCodeSets");
        } else {
            // Default to a terminology URI based on the OID
            entity.setFhirIdentifier("http://terminology.hl7.org/CodeSystem/oid-" + oidRoot);
        }
    }

    /**
     * Determine the appropriate service method name based on OID or code system
     */
    private String determineServiceMethodFromOid(String oid, String memberCodeSystem) {
        if (oid == null) {
            return "defaultProcessOid";
        }

        // Extract the OID root (before the colon if it exists)
        String oidRoot = oid;
        int colonIndex = oid.indexOf(':');
        if (colonIndex > 0) {
            oidRoot = oid.substring(0, colonIndex);
        }

        // Determine service method based on OID or member code system
        if (oidRoot.equals("2.16.840.1.113883.6.1") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("loinc"))) {
            return "processLoincOid";
        } else if (oidRoot.equals("2.16.840.1.113883.6.96") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("snomed"))) {
            return "processSnomedOid";
        } else if (oidRoot.equals("2.16.840.1.113883.6.88") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("rxnorm"))) {
            return "processRxNormOid";
        } else if (oidRoot.contains("2.16.840.1.113883.6.90") || oidRoot.contains("2.16.840.1.113883.6.103") ||
                (memberCodeSystem != null && memberCodeSystem.toLowerCase().contains("icd"))) {
            return "processIcdOid";
        } else if (oidRoot.equals("2.16.840.1.113883.6.12") || oidRoot.equals("2.16.840.1.113883.6.285") ||
                (memberCodeSystem != null && (memberCodeSystem.toLowerCase().contains("cpt") ||
                        memberCodeSystem.toLowerCase().contains("hcpcs")))) {
            return "processCptOid";
        } else {
            return "defaultProcessOid";
        }
    }
}