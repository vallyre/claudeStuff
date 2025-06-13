package com.mckesson.cmt.cmt_standardcode_gateway_service.dto;

import com.opencsv.bean.CsvBindByName;
import java.time.LocalDateTime;

/**
 * Class representing a row from a CSV file for OidMaster import
 * Modified to support both lower case and upper case headers
 */
public class OidCsvRecord {

    // Map to both upper and lower case variations of the header names
    @CsvBindByName(column = "CODE GROUP CONTENT SET")
    @CsvBindByName(column = "Code Group Content Set")
    private String codeGroupContentSet;

    @CsvBindByName(column = "CODE GROUP CONTENT SET VERSION")
    @CsvBindByName(column = "Code Group Content Set Version")
    private String codeGroupContentSetVersion;

    @CsvBindByName(column = "CODE SUB-TYPE")
    @CsvBindByName(column = "Code Sub-type")
    private String codeSubType;

    @CsvBindByName(column = "OID")
    private String oid;

    @CsvBindByName(column = "CODE GROUP NAME")
    @CsvBindByName(column = "Code Group Name")
    private String codeGroupName;

    @CsvBindByName(column = "CODE GROUP REVISION NAME")
    @CsvBindByName(column = "Code Group Revision Name")
    private String codeGroupRevisionName;

    @CsvBindByName(column = "MEMBER CODE SYSTEM")
    @CsvBindByName(column = "Member Code System")
    private String memberCodeSystem;

    @CsvBindByName(column = "DESCRIPTION")
    @CsvBindByName(column = "Description")
    private String description;

    @CsvBindByName(column = "REVISION START")
    @CsvBindByName(column = "Revision start")
    private String revisionStart;

    @CsvBindByName(column = "REVISION END")
    @CsvBindByName(column = "Revision end")
    private String revisionEnd;

    // Default constructor required for OpenCSV
    public OidCsvRecord() {
    }

    /**
     * Validates the CSV record
     * 
     * @return True if record is valid, false otherwise
     */
    public boolean isValid() {
        return oid != null && !oid.trim().isEmpty() &&
                codeGroupContentSet != null && !codeGroupContentSet.trim().isEmpty() &&
                codeGroupName != null && !codeGroupName.trim().isEmpty();
    }

    /**
     * Get validation error message if record is invalid
     * 
     * @return Error message or null if record is valid
     */
    public String getValidationErrorMessage() {
        if (oid == null || oid.trim().isEmpty()) {
            return "OID is required";
        }
        if (codeGroupContentSet == null || codeGroupContentSet.trim().isEmpty()) {
            return "Code Group Content Set is required";
        }
        if (codeGroupName == null || codeGroupName.trim().isEmpty()) {
            return "Code Group Name is required";
        }
        return null;
    }

    // Getters and setters
    public String getCodeGroupContentSet() {
        return codeGroupContentSet;
    }

    public void setCodeGroupContentSet(String codeGroupContentSet) {
        this.codeGroupContentSet = codeGroupContentSet;
    }

    public String getCodeGroupContentSetVersion() {
        return codeGroupContentSetVersion;
    }

    public void setCodeGroupContentSetVersion(String codeGroupContentSetVersion) {
        this.codeGroupContentSetVersion = codeGroupContentSetVersion;
    }

    public String getCodeSubType() {
        return codeSubType;
    }

    public void setCodeSubType(String codeSubType) {
        this.codeSubType = codeSubType;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getCodeGroupName() {
        return codeGroupName;
    }

    public void setCodeGroupName(String codeGroupName) {
        this.codeGroupName = codeGroupName;
    }

    public String getCodeGroupRevisionName() {
        return codeGroupRevisionName;
    }

    public void setCodeGroupRevisionName(String codeGroupRevisionName) {
        this.codeGroupRevisionName = codeGroupRevisionName;
    }

    public String getMemberCodeSystem() {
        return memberCodeSystem;
    }

    public void setMemberCodeSystem(String memberCodeSystem) {
        this.memberCodeSystem = memberCodeSystem;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRevisionStart() {
        return revisionStart;
    }

    public void setRevisionStart(String revisionStart) {
        this.revisionStart = revisionStart;
    }

    public String getRevisionEnd() {
        return revisionEnd;
    }

    public void setRevisionEnd(String revisionEnd) {
        this.revisionEnd = revisionEnd;
    }

    @Override
    public String toString() {
        return "OidCsvRecord{" +
                "oid='" + oid + '\'' +
                ", codeGroupContentSet='" + codeGroupContentSet + '\'' +
                ", codeGroupName='" + codeGroupName + '\'' +
                '}';
    }
}