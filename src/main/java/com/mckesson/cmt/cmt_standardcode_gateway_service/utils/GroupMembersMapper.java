package com.mckesson.cmt.cmt_standardcode_gateway_service.utils;

import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMembersHLIReq;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMembersReqDTO;

/**
 * Utility class to convert GroupMembersReqDTO to GroupMembersHLIReq
 */
public class GroupMembersMapper {

    /**
     * Converts a GroupMembersReqDTO object to a GroupMembersHLIReq object
     * 
     * @param dto the source GroupMembersReqDTO object
     * @return a new GroupMembersHLIReq object populated with values from the DTO
     */
    public static GroupMembersHLIReq toHLIRequest(GroupMembersReqDTO dto) {
        if (dto == null) {
            return null;
        }

        GroupMembersHLIReq hliReq = new GroupMembersHLIReq();

        // Transfer all properties from DTO to HLI request
        hliReq.setId(dto.getId());
        hliReq.setOid(dto.getOids() != null && !dto.getOids().isEmpty() ? dto.getOids().get(0) : "0");
        hliReq.setUrl(dto.getUrl());
        hliReq.setRevisionDate(dto.getRevisionDate());
        hliReq.setCount(dto.getCount());
        hliReq.setNextCursor(dto.getNextCursor());
        hliReq.setFields(dto.getFields());
        hliReq.setEffectiveDate(dto.getEffectiveDate());
        hliReq.setIncludeInvalid(dto.getIncludeInvalid());
        hliReq.setIncludeRetired(dto.getIncludeRetired());

        return hliReq;
    }

    /**
     * Example usage in a service or controller
     */
    public static void example() {
        // Sample usage in a service method
        GroupMembersReqDTO dto = new GroupMembersReqDTO();
        // ... set properties on dto

        // Convert DTO to HLI request
        GroupMembersHLIReq hliReq = GroupMembersMapper.toHLIRequest(dto);

        // Now use hliReq to make the HLI request
    }
}
