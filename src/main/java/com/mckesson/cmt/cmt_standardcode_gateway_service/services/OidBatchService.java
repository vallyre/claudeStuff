package com.mckesson.cmt.cmt_standardcode_gateway_service.services;

import com.mckesson.cmt.cmt_standardcode_gateway_service.dto.BatchUploadResult;
import com.mckesson.cmt.cmt_standardcode_gateway_service.dto.OidCsvRecord;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.HliApiConfig;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.HliApiConfigRepository;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.OidMasterRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for batch processing OidMaster records from CSV uploads
 */
@Service
public class OidBatchService {

    private static final Logger log = LoggerFactory.getLogger(OidBatchService.class);

    private final OidMasterRepository oidMasterRepository;
    private final HliApiConfigRepository hliApiConfigRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${hli.api.batch.default-size:100}")
    private int defaultBatchSize;

    @Autowired
    public OidBatchService(
            OidMasterRepository oidMasterRepository,
            HliApiConfigRepository hliApiConfigRepository) {
        this.oidMasterRepository = oidMasterRepository;
        this.hliApiConfigRepository = hliApiConfigRepository;
    }

    /**
     * Process a CSV file for OidMaster batch upload
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

        log.info("Processing OID CSV upload, batch size: {}", batchSize);

        // Validate batch size
        if (batchSize <= 0) {
            batchSize = defaultBatchSize;
        }

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

        // Parse CSV file using OpenCSV
        List<OidCsvRecord> records;
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<OidCsvRecord> csvToBean = new CsvToBeanBuilder<OidCsvRecord>(reader)
                    .withType(OidCsvRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            records = csvToBean.parse();
        }

        if (records.isEmpty()) {
            throw new IllegalArgumentException("CSV file does not contain any valid records");
        }

        log.info("Parsed {} records from OID CSV file", records.size());

        // Validate all records first
        List<OidCsvRecord> validRecords = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            OidCsvRecord record = records.get(i);
            if (record.isValid()) {
                validRecords.add(record);
            } else {
                String errorMsg = String.format("Row %d: %s", i + 2, record.getValidationErrorMessage());
                validationErrors.add(errorMsg);
            }
        }

        if (validRecords.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid records found in CSV. Errors: " + String.join("; ", validationErrors));
        }

        log.info("Found {} valid records out of {}", validRecords.size(), records.size());

        // Get existing OIDs to determine create vs update
        List<String> oids = validRecords.stream().map(OidCsvRecord::getOid).collect(Collectors.toList());
        Map<String, OidMaster> existingOidMap = oidMasterRepository.findByOidIn(oids).stream()
                .collect(Collectors.toMap(OidMaster::getOid, oid -> oid, (existing, replacement) -> existing));

        // Split records into batches
        List<List<OidCsvRecord>> batches = new ArrayList<>();
        for (int i = 0; i < validRecords.size(); i += batchSize) {
            batches.add(validRecords.subList(i, Math.min(i + batchSize, validRecords.size())));
        }

        log.info("Processing {} batches with batch size {}", batches.size(), batchSize);

        // Process each batch
        BatchUploadResult result = new BatchUploadResult();
        validationErrors.forEach(result::addErrorMessage);

        for (int batchNum = 0; batchNum < batches.size(); batchNum++) {
            List<OidCsvRecord> batch = batches.get(batchNum);
            log.info("Processing batch {}/{} with {} records", batchNum + 1, batches.size(), batch.size());

            List<OidMaster> entitiesToSave = new ArrayList<>();

            for (OidCsvRecord record : batch) {
                try {
                    OidMaster entity;
                    boolean isUpdate = existingOidMap.containsKey(record.getOid());

                    if (isUpdate) {
                        // Update existing entity
                        entity = existingOidMap.get(record.getOid());
                        updateEntityFromRecord(entity, record, username);
                        result.incrementUpdated();
                    } else {
                        // Create new entity
                        entity = createEntityFromRecord(record, hliApiConfig, username);
                        result.incrementCreated();
                    }

                    entitiesToSave.add(entity);
                } catch (Exception e) {
                    log.error("Error processing record: {}", record, e);
                    result.incrementFailed();
                    result.addErrorMessage("Error processing OID " + record.getOid() + ": " + e.getMessage());
                }
            }

            // Save batch
            if (!entitiesToSave.isEmpty()) {
                oidMasterRepository.saveAll(entitiesToSave);
                log.info("Saved batch {}/{} with {} records", batchNum + 1, batches.size(), entitiesToSave.size());
            }
        }

        log.info("Completed CSV processing: {}", result);
        return result;
    }

    /**
     * Create a new OidMaster entity from a CSV record
     */
    private OidMaster createEntityFromRecord(OidCsvRecord record, HliApiConfig hliApiConfig, String username) {
        OidMaster entity = new OidMaster();

        // Set required fields
        entity.setOid(record.getOid());
        entity.setCodeGroupContentSet(record.getCodeGroupContentSet());

        // Set content set version (may be a float in the CSV)
        if (record.getCodeGroupContentSetVersion() != null && !record.getCodeGroupContentSetVersion().isEmpty()) {
            entity.setCodeGroupContentSetVersion(record.getCodeGroupContentSetVersion());
        } else {
            entity.setCodeGroupContentSetVersion("1.0"); // Default
        }

        // Set code from the description field (if needed)
        entity.setCode(extractCodeFromOid(record.getOid()));
        entity.setCodeGroupName(record.getCodeGroupName());
        entity.setHliApiConfig(hliApiConfig);

        // Set optional fields if provided
        entity.setCodeSubType(record.getCodeSubType());
        entity.setCodeGroupRevisionName(record.getCodeGroupRevisionName());

        // Set FHIR identifier and HL7 URI based on OID
        setupIdentifiersFromOid(entity, record.getOid(), record.getMemberCodeSystem());

        // Parse revision start date
        if (record.getRevisionStart() != null && !record.getRevisionStart().isEmpty()) {
            try {
                entity.setRevisionStart(LocalDateTime.parse(record.getRevisionStart() + "T00:00:00"));
            } catch (DateTimeParseException e) {
                log.warn("Unable to parse revision start date: {}", record.getRevisionStart());
            }
        }

        // Set status to active by default
        entity.setIsActive(true);
        entity.setStatus("ACTIVE");

        // Determine appropriate service method name based on code system
        entity.setServiceMethodName(determineServiceMethodFromOid(record.getOid(), record.getMemberCodeSystem()));

        // Set audit fields
        entity.setCreatedBy(username);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedBy(username);
        entity.setLastModifiedDate(LocalDateTime.now());

        return entity;
    }

    /**
     * Update an existing OidMaster entity from a CSV record
     */
    private void updateEntityFromRecord(OidMaster entity, OidCsvRecord record, String username) {
        // Update fields that can be changed
        entity.setCodeGroupContentSet(record.getCodeGroupContentSet());

        // Update content set version if provided
        if (record.getCodeGroupContentSetVersion() != null && !record.getCodeGroupContentSetVersion().isEmpty()) {
            entity.setCodeGroupContentSetVersion(record.getCodeGroupContentSetVersion());
        }

        entity.setCodeSubType(record.getCodeSubType());
        entity.setCodeGroupName(record.getCodeGroupName());
        entity.setCodeGroupRevisionName(record.getCodeGroupRevisionName());

        // Update FHIR identifier and HL7 URI based on OID
        setupIdentifiersFromOid(entity, record.getOid(), record.getMemberCodeSystem());

        // Parse revision start date
        if (record.getRevisionStart() != null && !record.getRevisionStart().isEmpty()) {
            try {
                entity.setRevisionStart(LocalDateTime.parse(record.getRevisionStart() + "T00:00:00"));
            } catch (DateTimeParseException e) {
                log.warn("Unable to parse revision start date: {}", record.getRevisionStart());
            }
        }

        // Update service method name based on code system
        String serviceMethod = determineServiceMethodFromOid(record.getOid(), record.getMemberCodeSystem());
        if (serviceMethod != null) {
            entity.setServiceMethodName(serviceMethod);
        }

        // Update audit fields
        entity.setLastModifiedBy(username);
        entity.setLastModifiedDate(LocalDateTime.now());
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