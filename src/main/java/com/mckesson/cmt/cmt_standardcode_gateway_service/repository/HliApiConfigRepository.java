package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.HliApiConfig;

import java.util.List;
import java.util.Optional;

/**
 * Repository for HliApiConfig entities that provides CRUD operations
 */
@Repository
public interface HliApiConfigRepository extends JpaRepository<HliApiConfig, Long> {

    /**
     * Find an HliApiConfig by its config name
     * 
     * @param configName The config name to search for
     * @return An Optional containing the HliApiConfig if found
     */
    Optional<HliApiConfig> findByConfigName(String configName);

    /**
     * Find all active HliApiConfig records
     * 
     * @return A list of active HliApiConfig records
     */
    List<HliApiConfig> findByIsActiveTrue();

    /**
     * Find all HliApiConfig records with a specific API base URL
     * 
     * @param apiBaseUrl The API base URL to search for
     * @return A list of matching HliApiConfig records
     */
    List<HliApiConfig> findByApiBaseUrl(String apiBaseUrl);
}