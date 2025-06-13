package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OidMaster entities that provides CRUD operations
 */
@Repository
public interface OidMasterRepository extends JpaRepository<OidMaster, Long> {

    /**
     * Find an OidMaster by its OID string
     * 
     * @param oid The OID to search for
     * @return An Optional containing the OidMaster if found
     */
    Optional<OidMaster> findByOid(String oid);

    /**
     * Find all OidMaster records whose OID is in the provided list
     * 
     * @param oids The list of OIDs to search for
     * @return A list of matching OidMaster records
     */
    List<OidMaster> findByOidIn(List<String> oids);

    /**
     * Find all OidMaster records with a specific code
     * 
     * @param code The code to search for
     * @return A list of matching OidMaster records
     */
    List<OidMaster> findByCode(String code);

    /**
     * Find all OidMaster records with a specific code group name
     * 
     * @param codeGroupName The code group name to search for
     * @return A list of matching OidMaster records
     */
    List<OidMaster> findByCodeGroupName(String codeGroupName);

    /**
     * Find all active OidMaster records
     * 
     * @return A list of active OidMaster records
     */
    List<OidMaster> findByIsActiveTrue();

    /**
     * Find OidMaster records by HLI API Config ID
     * 
     * @param hliApiConfigId The HLI API Config ID to search for
     * @return A list of matching OidMaster records
     */
    List<OidMaster> findByHliApiConfig_Id(Long hliApiConfigId);

    /**
     * Find active OidMaster records by code group content set
     * 
     * @param codeGroupContentSet The code group content set to search for
     * @return A list of matching active OidMaster records
     */
    List<OidMaster> findByCodeGroupContentSetAndIsActiveTrue(String codeGroupContentSet);

    /**
     * Search for OidMaster records containing code or code group name like the
     * search term
     * 
     * @param searchTerm The search term to use
     * @return A list of matching OidMaster records
     */
    @Query("SELECT o FROM OidMaster o WHERE o.code LIKE %:searchTerm% OR o.codeGroupName LIKE %:searchTerm%")
    List<OidMaster> searchByCodeOrCodeGroupName(@Param("searchTerm") String searchTerm);

    /**
     * Custom query to find active OidMaster records by version and content set
     * 
     * @param contentSet The content set to search for
     * @param version    The version to search for
     * @return A list of matching OidMaster records
     */
    @Query("SELECT o FROM OidMaster o WHERE o.codeGroupContentSet = :contentSet " +
            "AND o.codeGroupContentSetVersion = :version AND o.isActive = true")
    List<OidMaster> findActiveByContentSetAndVersion(
            @Param("contentSet") String contentSet,
            @Param("version") String version);
}