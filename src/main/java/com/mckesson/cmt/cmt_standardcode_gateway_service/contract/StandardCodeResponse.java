package com.mckesson.cmt.cmt_standardcode_gateway_service.contract;



import java.time.ZonedDateTime;
import java.util.List;

public class StandardCodeResponse extends BaseResponse {
    private String responseId;
    private String requestId;
    private ZonedDateTime timestamp;
    private String status;
    private List<StandardCodeData> data;
    private ErrorDetails error;

    // Default constructor
    public StandardCodeResponse() {
        super();
    }

    // All args constructor
    public StandardCodeResponse(String responseId, String requestId, ZonedDateTime timestamp,
            String status, List<StandardCodeData> data, ErrorDetails error,
            boolean isSuccess, String message, List<Error> errors) {
        super(isSuccess, message, errors);
        this.responseId = responseId;
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.status = status;
        this.data = data;
        this.error = error;
    }

    // Getters and setters
    public String getResponseId() {
        return this.responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ZonedDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<StandardCodeData> getData() {
        return this.data;
    }

    public void setData(List<StandardCodeData> data) {
        this.data = data;
    }

    public ErrorDetails getError() {
        return this.error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    // Inner classes
    public static class StandardCodeData {
        private String oId;
        private String revision;
        private List<StandardCodeResult> results;
        private String nextCursor;

        // Constructors
        public StandardCodeData() {
        }

        public StandardCodeData(String oId, String revision, List<StandardCodeResult> results, String nextCursor) {
            this.oId = oId;
            this.revision = revision;
            this.results = results;
            this.nextCursor = nextCursor;
        }

        // Getters and setters
        public String getOId() {
            return oId;
        }

        public void setOId(String oId) {
            this.oId = oId;
        }

        public String getRevision() {
            return revision;
        }

        public void setRevision(String revision) {
            this.revision = revision;
        }

        public List<StandardCodeResult> getResults() {
            return results;
        }

        public void setResults(List<StandardCodeResult> results) {
            this.results = results;
        }

        public String getNextCursor() {
            return nextCursor;
        }

        public void setNextCursor(String nextCursor) {
            this.nextCursor = nextCursor;
        }
    }

    public static class StandardCodeResult {
        private Boolean isQuestionnaire;
        private String id;
        private String name;
        private String code;
        private String definition;
        private String codeSystemId;
        private List<Option> options;

        // Constructors
        public StandardCodeResult() {
        }

        public StandardCodeResult(Boolean isQuestionnaire, String id, String name, String code,
                String definition, String codeSystemId, List<Option> options) {
            this.isQuestionnaire = isQuestionnaire;
            this.id = id;
            this.name = name;
            this.code = code;
            this.definition = definition;
            this.codeSystemId = codeSystemId;
            this.options = options;
        }

        // Getters and setters
        public Boolean getIsQuestionnaire() {
            return isQuestionnaire;
        }

        public void setIsQuestionnaire(Boolean isQuestionnaire) {
            this.isQuestionnaire = isQuestionnaire;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public String getCodeSystemId() {
            return codeSystemId;
        }

        public void setCodeSystemId(String codeSystemId) {
            this.codeSystemId = codeSystemId;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }
    }

    public static class Option {
        private String id;
        private String name;
        private String code;

        // Constructors
        public Option() {
        }

        public Option(String id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

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