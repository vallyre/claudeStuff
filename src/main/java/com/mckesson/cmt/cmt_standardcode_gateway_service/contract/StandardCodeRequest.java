package com.mckesson.cmt.cmt_standardcode_gateway_service.contract;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StandardCodeRequest extends BaseRequest {
    private String operation;
    private Parameters parameters;
    private String priority;

    // Default constructor
    public StandardCodeRequest() {
        super();
    }

    // All args constructor
    public StandardCodeRequest(String requestId, String operation, Parameters parameters, String priority) {
        super(requestId);
        this.operation = operation;
        this.parameters = parameters;
        this.priority = priority;
    }

    // Getters and setters
    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Parameters getParameters() {
        return this.parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public String getPriority() {
        return this.priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    // Inner classes
    public static class Parameters {
        private List<OidRevision> oidRevisions;

        // Constructors
        public Parameters() {
        }

        public Parameters(List<OidRevision> oidRevisions) {
            this.oidRevisions = oidRevisions;
        }

        // Getters and setters
        public List<OidRevision> getOidRevisions() {
            return oidRevisions;
        }

        public void setOidRevisions(List<OidRevision> oidRevisions) {
            this.oidRevisions = oidRevisions;
        }
    }

    public static class OidRevision {
        @JsonProperty("oId")
        private String oId;
        private String revision;

        // Constructors
        public OidRevision() {
        }

        public OidRevision(String oId, String revision) {
            this.oId = oId;
            this.revision = revision;
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
    }
}