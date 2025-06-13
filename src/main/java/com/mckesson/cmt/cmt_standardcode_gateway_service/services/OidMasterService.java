package com.mckesson.cmt.cmt_standardcode_gateway_service.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.StandardCodeResponse;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.OidMasterRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing OidMaster entities
 */
@Service
public class OidMasterService {

    private final OidMasterRepository oidMasterRepository;

    @Autowired
    public OidMasterService(OidMasterRepository oidMasterRepository) {
        this.oidMasterRepository = oidMasterRepository;
    }

    /**
     * Get all OidMaster records
     * 
     * @return List of all OidMaster records
     */
    public List<OidMaster> findAll() {
        return oidMasterRepository.findAll();
    }

    /**
     * Get an OidMaster by its ID
     * 
     * @param id The ID to look up
     * @return Optional containing the OidMaster if found
     */
    public Optional<OidMaster> findById(Long id) {
        return oidMasterRepository.findById(id);
    }

    /**
     * Find all OidMaster records matching the given list of OIDs
     * 
     * @param oids The list of OIDs to search for
     * @return A list of matching OidMaster records
     */
    public List<OidMaster> findByOids(List<String> oids) {
        if (oids == null || oids.isEmpty()) {
            return Collections.emptyList();
        }
        return oidMasterRepository.findByOidIn(oids);
    }

    /**
     * Get an OidMaster by its OID string
     * 
     * @param oid The OID string to look up
     * @return Optional containing the OidMaster if found
     */
    public Optional<OidMaster> findByOid(String oid) {
        return oidMasterRepository.findByOid(oid);
    }

    /**
     * Find all OidMaster records with a specific code
     * 
     * @param code The code to search for
     * @return A list of matching OidMaster records
     */
    public List<OidMaster> findByCode(String code) {
        return oidMasterRepository.findByCode(code);
    }

    /**
     * Find all active OidMaster records
     * 
     * @return A list of active OidMaster records
     */
    public List<OidMaster> findAllActive() {
        return oidMasterRepository.findByIsActiveTrue();
    }

    /**
     * Save a new OidMaster or update an existing one
     * 
     * @param oidMaster The OidMaster to save
     * @param username  The username of the person making the change
     * @return The saved OidMaster
     */
    @Transactional
    public OidMaster save(OidMaster oidMaster, String username) {
        if (oidMaster.getId() == null) {
            // New entity
            oidMaster.setCreatedBy(username);
            oidMaster.setCreatedDate(LocalDateTime.now());
        } else {
            // Existing entity - update modification info
            oidMaster.setLastModifiedBy(username);
            oidMaster.setLastModifiedDate(LocalDateTime.now());
        }
        return oidMasterRepository.save(oidMaster);
    }

    /**
     * Create a new OidMaster
     * 
     * @param oidMaster The OidMaster to create
     * @param username  The username of the creator
     * @return The created OidMaster
     */
    @Transactional
    public OidMaster create(OidMaster oidMaster, String username) {
        oidMaster.setCreatedBy(username);
        oidMaster.setCreatedDate(LocalDateTime.now());
        oidMaster.setIsActive(true);
        oidMaster.setStatus("ACTIVE");
        return oidMasterRepository.save(oidMaster);
    }

    /**
     * Update an existing OidMaster
     * 
     * @param id               The ID of the OidMaster to update
     * @param updatedOidMaster The updated OidMaster data
     * @param username         The username of the person making the update
     * @return Optional containing the updated OidMaster if found
     */
    @Transactional
    public Optional<OidMaster> update(Long id, OidMaster updatedOidMaster, String username) {
        return oidMasterRepository.findById(id)
                .map(existingOidMaster -> {
                    // Update fields from updatedOidMaster
                    existingOidMaster.setOid(updatedOidMaster.getOid());
                    existingOidMaster.setCodeGroupContentSet(updatedOidMaster.getCodeGroupContentSet());
                    existingOidMaster.setCodeGroupContentSetVersion(updatedOidMaster.getCodeGroupContentSetVersion());
                    existingOidMaster.setCodeSubType(updatedOidMaster.getCodeSubType());
                    existingOidMaster.setFhirIdentifier(updatedOidMaster.getFhirIdentifier());
                    existingOidMaster.setHl7Uri(updatedOidMaster.getHl7Uri());
                    existingOidMaster.setCode(updatedOidMaster.getCode());
                    existingOidMaster.setCodeGroupName(updatedOidMaster.getCodeGroupName());
                    existingOidMaster.setCodeGroupRevisionName(updatedOidMaster.getCodeGroupRevisionName());
                    existingOidMaster.setRevisionStart(updatedOidMaster.getRevisionStart());
                 //   existingOidMaster.setHliApiConfigId(updatedOidMaster.getHliApiConfigId());
                    existingOidMaster.setIsActive(updatedOidMaster.getIsActive());
                    existingOidMaster.setStatus(updatedOidMaster.getStatus());

                    // Update audit fields
                    existingOidMaster.setLastModifiedBy(username);
                    existingOidMaster.setLastModifiedDate(LocalDateTime.now());

                    return oidMasterRepository.save(existingOidMaster);
                });
    }

    /**
     * Delete an OidMaster by its ID
     * 
     * @param id The ID of the OidMaster to delete
     */
    @Transactional
    public void deleteById(Long id) {
        oidMasterRepository.deleteById(id);
    }

    /**
     * Deactivate an OidMaster instead of deleting it
     * 
     * @param id       The ID of the OidMaster to deactivate
     * @param username The username of the person making the change
     * @return Optional containing the deactivated OidMaster if found
     */
    @Transactional
    public Optional<OidMaster> deactivate(Long id, String username) {
        return oidMasterRepository.findById(id)
                .map(oidMaster -> {
                    oidMaster.setIsActive(false);
                    oidMaster.setStatus("INACTIVE");
                    oidMaster.setLastModifiedBy(username);
                    oidMaster.setLastModifiedDate(LocalDateTime.now());
                    return oidMasterRepository.save(oidMaster);
                });
    }

    /**
     * Search for OidMaster records containing code or code group name like the
     * search term
     * 
     * @param searchTerm The search term to use
     * @return A list of matching OidMaster records
     */
    public List<OidMaster> searchByCodeOrCodeGroupName(String searchTerm) {
        return oidMasterRepository.searchByCodeOrCodeGroupName(searchTerm);
    }

    /**
     * Find OidMaster records by HLI API Config ID
     * 
     * @param hliApiConfigId The HLI API Config ID to search for
     * @return A list of matching OidMaster records
     */
    public List<OidMaster> findByHliApiConfigId(Long hliApiConfigId) {
        return oidMasterRepository.findByHliApiConfig_Id(hliApiConfigId);
    }

    /**
     * Find active OidMaster records by content set and version
     * 
     * @param contentSet The content set to search for
     * @param version    The version to search for
     * @return A list of matching active OidMaster records
     */
    public List<OidMaster> findActiveByContentSetAndVersion(String contentSet, String version) {
        return oidMasterRepository.findActiveByContentSetAndVersion(contentSet, version);
    }

    public Optional<StandardCodeResponse> findConsumerResponseByOid(String oid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findConsumerResponseByOid'");
    }
}