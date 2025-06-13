package com.mckesson.cmt.cmt_standardcode_gateway_service.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.HliApiConfig;
import com.mckesson.cmt.cmt_standardcode_gateway_service.repository.HliApiConfigRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing HliApiConfig entities
 */
@Service
public class HliApiConfigService {

    private final HliApiConfigRepository hliApiConfigRepository;

    @Autowired
    public HliApiConfigService(HliApiConfigRepository hliApiConfigRepository) {
        this.hliApiConfigRepository = hliApiConfigRepository;
    }

    /**
     * Get all HliApiConfig records
     * 
     * @return List of all HliApiConfig records
     */
    public List<HliApiConfig> findAll() {
        return hliApiConfigRepository.findAll();
    }

    /**
     * Get all active HliApiConfig records
     * 
     * @return List of all active HliApiConfig records
     */
    public List<HliApiConfig> findAllActive() {
        return hliApiConfigRepository.findByIsActiveTrue();
    }

    /**
     * Get an HliApiConfig by its ID
     * 
     * @param id The ID to look up
     * @return Optional containing the HliApiConfig if found
     */
    public Optional<HliApiConfig> findById(Long id) {
        return hliApiConfigRepository.findById(id);
    }

    /**
     * Get an HliApiConfig by its config name
     * 
     * @param configName The config name to look up
     * @return Optional containing the HliApiConfig if found
     */
    public Optional<HliApiConfig> findByConfigName(String configName) {
        return hliApiConfigRepository.findByConfigName(configName);
    }

    /**
     * Save a new HliApiConfig or update an existing one
     * 
     * @param hliApiConfig The HliApiConfig to save
     * @param username     The username of the person making the change
     * @return The saved HliApiConfig
     */
    @Transactional
    public HliApiConfig save(HliApiConfig hliApiConfig, String username) {
        if (hliApiConfig.getId() == null) {
            // New entity
            hliApiConfig.setCreatedBy(username);
            hliApiConfig.setCreatedAt(LocalDateTime.now());
        } else {
            // Existing entity - update modification info
           // hliApiConfig.setLastModifiedBy(username);
          //  hliApiConfig.setLastModifiedDate(LocalDateTime.now());
        }
        return hliApiConfigRepository.save(hliApiConfig);
    }

    /**
     * Create a new HliApiConfig
     * 
     * @param hliApiConfig The HliApiConfig to create
     * @param username     The username of the creator
     * @return The created HliApiConfig
     */
    @Transactional
    public HliApiConfig create(HliApiConfig hliApiConfig, String username) {
        hliApiConfig.setCreatedBy(username);
        hliApiConfig.setCreatedAt(LocalDateTime.now());
        hliApiConfig.setIsActive(true);
        return hliApiConfigRepository.save(hliApiConfig);
    }

    /**
     * Update an existing HliApiConfig
     * 
     * @param id                  The ID of the HliApiConfig to update
     * @param updatedHliApiConfig The updated HliApiConfig data
     * @param username            The username of the person making the update
     * @return Optional containing the updated HliApiConfig if found
     */
    @Transactional
    public Optional<HliApiConfig> update(Long id, HliApiConfig updatedHliApiConfig, String username) {
        return hliApiConfigRepository.findById(id)
                .map(existingConfig -> {
                    // Update fields from updatedHliApiConfig
                    existingConfig.setConfigName(updatedHliApiConfig.getConfigName());
                    existingConfig.setApiBaseUrl(updatedHliApiConfig.getApiBaseUrl());
                   // existingConfig.setApiKey(updatedHliApiConfig.getApiKey());
                   // existingConfig.setApiSecret(updatedHliApiConfig.getApiSecret());
                    existingConfig.setIsActive(updatedHliApiConfig.getIsActive());

                    // Update audit fields
                   // existingConfig.setLastModifiedBy(username);
                  //  existingConfig.setLastModifiedDate(LocalDateTime.now());

                    return hliApiConfigRepository.save(existingConfig);
                });
    }

    /**
     * Delete an HliApiConfig by its ID
     * 
     * @param id The ID of the HliApiConfig to delete
     */
    @Transactional
    public void deleteById(Long id) {
        hliApiConfigRepository.deleteById(id);
    }

    /**
     * Deactivate an HliApiConfig instead of deleting it
     * 
     * @param id       The ID of the HliApiConfig to deactivate
     * @param username The username of the person making the change
     * @return Optional containing the deactivated HliApiConfig if found
     */
    @Transactional
    public Optional<HliApiConfig> deactivate(Long id, String username) {
        return hliApiConfigRepository.findById(id)
                .map(config -> {
                    config.setIsActive(false);
                    //config.setLastModifiedBy(username);
                    //config.setLastModifiedDate(LocalDateTime.now());
                    return hliApiConfigRepository.save(config);
                });
    }
}