package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidHliApiResponse;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;

import java.util.List;
import java.util.Optional;

/**
 * Repository for OidHliApiResponse entities that provides CRUD operations
 */
@Repository
public interface OidHliApiResponseRepository extends JpaRepository<OidHliApiResponse, Integer> {

    /**
     * Find the current OidHliApiResponse for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return An Optional containing the current OidHliApiResponse if found
     */
    Optional<OidHliApiResponse> findByOidMasterAndIsCurrentTrue(OidMaster oidMaster);

    /**
     * Find all OidHliApiResponse records for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return A list of matching OidHliApiResponse records
     */
    List<OidHliApiResponse> findByOidMaster(OidMaster oidMaster);

    /**
     * Find all OidHliApiResponse records for a specific OidMaster, ordered by
     * version descending
     * 
     * @param oidMaster The OidMaster entity
     * @return A list of matching OidHliApiResponse records
     */
    List<OidHliApiResponse> findByOidMasterOrderByVersionDesc(OidMaster oidMaster);

    /**
     * Find OidHliApiResponse by OidMaster and version
     * 
     * @param oidMaster The OidMaster entity
     * @param version   The version to search for
     * @return An Optional containing the OidHliApiResponse if found
     */
    Optional<OidHliApiResponse> findByOidMasterAndVersion(OidMaster oidMaster, Integer version);

    /**
     * Find all OidHliApiResponse records with a specific HTTP status code
     * 
     * @param httpStatusCode The HTTP status code to search for
     * @return A list of matching OidHliApiResponse records
     */
    List<OidHliApiResponse> findByHttpStatusCode(Integer httpStatusCode);
}