package com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups;

import java.util.List;

public class GroupMemberHLIRes {

    private List<CodingInfo> results;
    private String nextCursor;

    // Getters and setters
    public List<CodingInfo> getResults() {
        return results;
    }

    public void setResults(List<CodingInfo> results) {
        this.results = results;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

}
