package com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups;

import java.util.List;

public class GroupMemberResDTO {
    private List<String> oids;


    public List<String> getOids() {
        return this.oids;
    }

    public void setOids(List<String> oids) {
        this.oids = oids;
    }

}
