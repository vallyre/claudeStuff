package com.mckesson.cmt.cmt_standardcode_gateway_service.services;

import org.springframework.stereotype.Service;

import com.mckesson.cmt.cmt_standardcode_gateway_service.client.HLIRestAPIClient;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMemberHLIRes;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMembersReqDTO;

import reactor.core.publisher.Mono;

@Service
public class StandardCodesHLIService {

    private final HLIRestAPIClient hliRestAPIClient;

    public StandardCodesHLIService(HLIRestAPIClient hliRestAPIClient){
        this.hliRestAPIClient = hliRestAPIClient;

    }

     public Mono<GroupMemberHLIRes> sendHLIRequest(GroupMembersReqDTO groupMembersReqDTO) {
        return null;


     }

     // Get Group Members
     // If OID is simple list or Collection/Questions&Answers --> Seperate into two methods based on the config table


}
