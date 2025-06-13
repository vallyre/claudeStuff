package com.mckesson.cmt.cmt_standardcode_gateway_service.services;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.StandardCodesMaster;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.StandardCodesResponse;
import com.mckesson.cmt.cmt_standardcode_gateway_service.exception.ResourceNotFoundException;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.StandardCodesMasterRepository;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.StandardCodesResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardCodesMasterService {

    private final StandardCodesMasterRepository masterRepository;
    private final StandardCodesResponseRepository responseRepository;

    @Transactional(readOnly = true)
    public StandardCodesMaster getMasterByUuid(UUID masterUuid) {
        return masterRepository.findByMasterUuid(masterUuid)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Standard code master not found with UUID: " + masterUuid));
    }

    @Transactional(readOnly = true)
    public List<StandardCodesMaster> getActiveMastersByResourceType(String resourceType) {
        return masterRepository.findByResourceTypeAndStatus(resourceType, "active");
    }

    @Transactional
    public StandardCodesMaster createMaster(StandardCodesMaster master) {
        master.setMasterUuid(UUID.randomUUID());
        master.setCreatedDatetime(LocalDateTime.now());
        master.setUpdatedDatetime(LocalDateTime.now());
        return masterRepository.save(master);
    }

    @Transactional
    public StandardCodesMaster updateMaster(UUID masterUuid, StandardCodesMaster updatedMaster) {
        StandardCodesMaster existingMaster = getMasterByUuid(masterUuid);

        // Update fields
        existingMaster.setResourceType(updatedMaster.getResourceType());
        existingMaster.setName(updatedMaster.getName());
        existingMaster.setVersion(updatedMaster.getVersion());
        existingMaster.setStatus(updatedMaster.getStatus());
        existingMaster.setTitle(updatedMaster.getTitle());
        existingMaster.setPublisher(updatedMaster.getPublisher());
        existingMaster.setUrl(updatedMaster.getUrl());
        existingMaster.setDescription(updatedMaster.getDescription());
        existingMaster.setPurpose(updatedMaster.getPurpose());
        existingMaster.setDate(updatedMaster.getDate());
        existingMaster.setApprovalDate(updatedMaster.getApprovalDate());
        existingMaster.setLastReviewDate(updatedMaster.getLastReviewDate());
        existingMaster.setExperimental(updatedMaster.getExperimental());
        existingMaster.setUpdatedDatetime(LocalDateTime.now());
        existingMaster.setUpdatedBy(updatedMaster.getUpdatedBy());

        return masterRepository.save(existingMaster);
    }

    @Transactional
    public void deactivateMaster(UUID masterUuid) {
        StandardCodesMaster master = getMasterByUuid(masterUuid);
        master.setEffectiveEndDate(LocalDateTime.now());
        master.setStatus("inactive");
        master.setUpdatedDatetime(LocalDateTime.now());
        masterRepository.save(master);
    }

    @Transactional
    public StandardCodesResponse addResponse(UUID masterUuid, StandardCodesResponse response) {
        StandardCodesMaster master = getMasterByUuid(masterUuid);

        response.setVersionUuid(UUID.randomUUID());
        response.setMasterUuid(masterUuid);
        response.setStandardCodesMaster(master);
        response.setCreatedDatetime(LocalDateTime.now());
        response.setUpdatedDatetime(LocalDateTime.now());

        return responseRepository.save(response);
    }

    @Transactional(readOnly = true)
    public List<StandardCodesResponse> getResponseHistory(UUID masterUuid) {
        return responseRepository.findByMasterUuidOrderByVersionDesc(masterUuid);
    }

    @Transactional(readOnly = true)
    public StandardCodesResponse getActiveResponse(UUID masterUuid) {
        return responseRepository.findByMasterUuidAndNoEffectiveEndDate(masterUuid)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No active response found for master UUID: " + masterUuid));
    }

    @Transactional
    public StandardCodesResponse updateResponse(UUID versionUuid, String apiResponse) {
        StandardCodesResponse response = responseRepository.findByVersionUuid(versionUuid)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Response not found with version UUID: " + versionUuid));

        response.setApiResponse(apiResponse);
        response.setUpdatedDatetime(LocalDateTime.now());

        return responseRepository.save(response);
    }

    @Transactional
    public List<StandardCodesResponse> getResponsesByMasterUuids(List<UUID> masterUuids) {
        return masterUuids.stream()
                .map(this::getActiveResponse)
                .toList();
    }
}