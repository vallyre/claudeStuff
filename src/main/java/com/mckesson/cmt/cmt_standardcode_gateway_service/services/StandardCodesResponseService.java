package com.mckesson.cmt.cmt_standardcode_gateway_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mckesson.cmt.cmt_standardcode_gateway_service.model.JsonResponse;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.StandardCodesResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardCodesResponseService {

    private final StandardCodesResponseRepository responseRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public JsonResponse getResponses(List<UUID> uuids) {
        log.debug("Processing {} UUIDs", uuids.size());

        // Get responses from both queries
        List<String> masterResponses = responseRepository.findActiveResponsesByMasterUuids(
                uuids.toArray(UUID[]::new));
        log.debug("Found {} responses from master UUIDs", masterResponses.size());

        List<String> versionResponses = responseRepository.findResponsesByVersionUuids(
                uuids.toArray(UUID[]::new));
        log.debug("Found {} responses from version UUIDs", versionResponses.size());

        // Combine both response lists while avoiding duplicates
        Set<String> uniqueResponses = new HashSet<>();
        uniqueResponses.addAll(masterResponses);
        uniqueResponses.addAll(versionResponses);

        // Convert responses to JsonNodes
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (String response : uniqueResponses) {
            try {
                JsonNode node = objectMapper.readTree(response);
                if (node != null) {
                    jsonNodes.add(node);
                }
            } catch (Exception e) {
                log.error("Error parsing JSON response: {}", e.getMessage());
            }
        }

        log.debug("Total unique responses found: {}", jsonNodes.size());

        return JsonResponse.builder()
                .responseId(UUID.randomUUID().toString())
                .requestId(UUID.randomUUID().toString())
                .timestamp(ZonedDateTime.now())
                .status("SUCCESS")
                .responses(jsonNodes)
                .build();
    }
}