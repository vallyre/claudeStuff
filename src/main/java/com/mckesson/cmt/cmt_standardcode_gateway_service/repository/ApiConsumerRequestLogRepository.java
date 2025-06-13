package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.ApiConsumerRequestLog;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.ApiConsumerRequestType;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ApiConsumerRequestLog entities that provides CRUD operations
 */
@Repository
public interface ApiConsumerRequestLogRepository extends JpaRepository<ApiConsumerRequestLog, Integer> {

    /**
     * Find an ApiConsumerRequestLog by its request ID
     * 
     * @param requestId The request ID to search for
     * @return An Optional containing the ApiConsumerRequestLog if found
     */
    Optional<ApiConsumerRequestLog> findByRequestId(String requestId);

    /**
     * Find all ApiConsumerRequestLog records for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return A list of matching ApiConsumerRequestLog records
     */
    List<ApiConsumerRequestLog> findByOidMaster(OidMaster oidMaster);

    /**
     * Find all ApiConsumerRequestLog records for a specific request type
     * 
     * @param requestType The ApiConsumerRequestType entity
     * @return A list of matching ApiConsumerRequestLog records
     */
    List<ApiConsumerRequestLog> findByRequestType(ApiConsumerRequestType requestType);

    /**
     * Find all ApiConsumerRequestLog records with a specific HTTP status code
     * 
     * @param httpStatusCode The HTTP status code to search for
     * @return A list of matching ApiConsumerRequestLog records
     */
    List<ApiConsumerRequestLog> findByHttpStatusCode(Integer httpStatusCode);

    /**
     * Find all ApiConsumerRequestLog records with a specific cache hit status
     * 
     * @param cacheHit Boolean indicating if there was a cache hit
     * @return A list of matching ApiConsumerRequestLog records
     */
    List<ApiConsumerRequestLog> findByCacheHit(Boolean cacheHit);

    /**
     * Find all ApiConsumerRequestLog records within a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return A list of matching ApiConsumerRequestLog records
     */
    List<ApiConsumerRequestLog> findByRequestTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all ApiConsumerRequestLog records for a specific IP address
     * 
     * @param requestIp The IP address to search for
     * @return A list of matching ApiConsumerRequestLog records
     */
    List<ApiConsumerRequestLog> findByRequestIp(String requestIp);

    /**
     * Count the number of requests by request type in a date range
     * 
     * @param requestType The request type to count
     * @param startDate   The start date of the range
     * @param endDate     The end date of the range
     * @return The count of matching requests
     */
    @Query("SELECT COUNT(r) FROM ApiConsumerRequestLog r WHERE r.requestType = :requestType " +
            "AND r.requestTimestamp BETWEEN :startDate AND :endDate")
    Long countByRequestTypeAndDateRange(
            @Param("requestType") ApiConsumerRequestType requestType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find the average response time for a specific OID
     * 
     * @param oidMaster The OidMaster entity
     * @return The average response time in milliseconds
     */
    @Query("SELECT AVG(r.responseTimeMs) FROM ApiConsumerRequestLog r WHERE r.oidMaster = :oidMaster")
    Double findAverageResponseTimeByOidMaster(@Param("oidMaster") OidMaster oidMaster);

    /**
     * Find the cache hit rate for a specific OID
     * 
     * @param oidMaster The OidMaster entity
     * @return The cache hit rate as a percentage
     */
    @Query("SELECT (COUNT(CASE WHEN r.cacheHit = true THEN 1 ELSE null END) * 100.0 / COUNT(r)) " +
            "FROM ApiConsumerRequestLog r WHERE r.oidMaster = :oidMaster")
    Double findCacheHitRateByOidMaster(@Param("oidMaster") OidMaster oidMaster);
}