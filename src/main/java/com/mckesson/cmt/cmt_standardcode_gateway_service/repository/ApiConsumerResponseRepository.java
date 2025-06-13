package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.ApiConsumerResponse;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ApiConsumerResponse entities that provides CRUD operations
 */
@Repository
public interface ApiConsumerResponseRepository extends JpaRepository<ApiConsumerResponse, Integer> {

    /**
     * Find the current ApiConsumerResponse for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return An Optional containing the current ApiConsumerResponse if found
     */
    Optional<ApiConsumerResponse> findByOidMasterAndIsCurrentTrue(OidMaster oidMaster);

    /**
     * Find all ApiConsumerResponse records for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return A list of matching ApiConsumerResponse records
     */
    List<ApiConsumerResponse> findByOidMaster(OidMaster oidMaster);

    /**
     * Find all ApiConsumerResponse records for a specific OidMaster, ordered by
     * version descending
     * 
     * @param oidMaster The OidMaster entity
     * @return A list of matching ApiConsumerResponse records
     */
    List<ApiConsumerResponse> findByOidMasterOrderByVersionDesc(OidMaster oidMaster);

    /**
     * Find ApiConsumerResponse by OidMaster and version
     * 
     * @param oidMaster The OidMaster entity
     * @param version   The version to search for
     * @return An Optional containing the ApiConsumerResponse if found
     */
    Optional<ApiConsumerResponse> findByOidMasterAndVersion(OidMaster oidMaster, Integer version);
}