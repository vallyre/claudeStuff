package com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups;

import java.util.List;

public class GroupMembersReqDTO {


    private String id;
    private List<String> oids;
    private String url;
    private String revisionDate; //YYYY-MM-DD
    private Integer count;
    private String nextCursor;
    private List<String> fields;
    private String effectiveDate;
    private String includeInvalid; //false OR true
    private String includeRetired; //false OR true


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getOids() {
        return this.oids;
    }

    public void setOids(List<String> oids) {
        this.oids = oids;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRevisionDate() {
        return this.revisionDate;
    }

    public void setRevisionDate(String revisionDate) {
        this.revisionDate = revisionDate;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNextCursor() {
        return this.nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public List<String> getFields() {
        return this.fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getEffectiveDate() {
        return this.effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getIncludeInvalid() {
        return this.includeInvalid;
    }

    public void setIncludeInvalid(String includeInvalid) {
        this.includeInvalid = includeInvalid;
    }

    public String getIncludeRetired() {
        return this.includeRetired;
    }

    public void setIncludeRetired(String includeRetired) {
        this.includeRetired = includeRetired;
    }

}
