package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.ApiConsumerRequestType;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ApiConsumerRequestType entities that provides CRUD operations
 */
@Repository
public interface ApiConsumerRequestTypeRepository extends JpaRepository<ApiConsumerRequestType, Integer> {

    /**
     * Find an ApiConsumerRequestType by its type name
     * 
     * @param typeName The type name to search for
     * @return An Optional containing the ApiConsumerRequestType if found
     */
    Optional<ApiConsumerRequestType> findByTypeName(String typeName);

    /**
     * Find all active ApiConsumerRequestType records
     * 
     * @return A list of active ApiConsumerRequestType records
     */
    List<ApiConsumerRequestType> findByIsActiveTrue();

    /**
     * Find all ApiConsumerRequestType records that require an OID
     * 
     * @param requiresOid Boolean indicating if the request type requires an OID
     * @return A list of matching ApiConsumerRequestType records
     */
    List<ApiConsumerRequestType> findByRequiresOid(Boolean requiresOid);

    /**
     * Find all active ApiConsumerRequestType records that require an OID
     * 
     * @param requiresOid Boolean indicating if the request type requires an OID
     * @return A list of matching active ApiConsumerRequestType records
     */
    List<ApiConsumerRequestType> findByRequiresOidAndIsActiveTrue(Boolean requiresOid);

    /**
     * Find active ApiConsumerRequestType by cache TTL greater than specified value
     * 
     * @param cacheTtlSeconds The minimum cache TTL in seconds
     * @return A list of matching active ApiConsumerRequestType records
     */
    List<ApiConsumerRequestType> findByIsActiveTrueAndCacheTtlSecondsGreaterThan(Integer cacheTtlSeconds);
}