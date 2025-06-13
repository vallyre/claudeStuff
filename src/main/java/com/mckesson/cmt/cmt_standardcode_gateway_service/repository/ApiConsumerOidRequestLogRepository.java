package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.ApiConsumerOidRequestLog;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidMaster;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ApiConsumerOidRequestLog entities that provides CRUD
 * operations
 */
@Repository
public interface ApiConsumerOidRequestLogRepository extends JpaRepository<ApiConsumerOidRequestLog, Integer> {

    /**
     * Find all ApiConsumerOidRequestLog records for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return A list of matching ApiConsumerOidRequestLog records
     */
    List<ApiConsumerOidRequestLog> findByOidMaster(OidMaster oidMaster);

    /**
     * Find all ApiConsumerOidRequestLog records with a specific success status
     * 
     * @param success Boolean indicating if the request was successful
     * @return A list of matching ApiConsumerOidRequestLog records
     */
    List<ApiConsumerOidRequestLog> findBySuccess(Boolean success);

    /**
     * Find all ApiConsumerOidRequestLog records with a specific HTTP status code
     * 
     * @param httpStatusCode The HTTP status code to search for
     * @return A list of matching ApiConsumerOidRequestLog records
     */
    List<ApiConsumerOidRequestLog> findByHttpStatusCode(Integer httpStatusCode);

    /**
     * Find all ApiConsumerOidRequestLog records with retry count greater than
     * specified value
     * 
     * @param retryCount The minimum retry count to search for
     * @return A list of matching ApiConsumerOidRequestLog records
     */
    List<ApiConsumerOidRequestLog> findByRetryCountGreaterThan(Integer retryCount);

    /**
     * Find all ApiConsumerOidRequestLog records within a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return A list of matching ApiConsumerOidRequestLog records
     */
    List<ApiConsumerOidRequestLog> findByRequestTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all ApiConsumerOidRequestLog records for a specific OidMaster ordered by
     * request timestamp descending
     * 
     * @param oidMaster The OidMaster entity
     * @return A list of matching ApiConsumerOidRequestLog records
     */
    List<ApiConsumerOidRequestLog> findByOidMasterOrderByRequestTimestampDesc(OidMaster oidMaster);

    /**
     * Find the average response time for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return The average response time in milliseconds
     */
    @Query("SELECT AVG(r.responseTimeMs) FROM ApiConsumerOidRequestLog r WHERE r.oidMaster = :oidMaster AND r.success = true")
    Double findAverageResponseTimeByOidMaster(@Param("oidMaster") OidMaster oidMaster);

    /**
     * Find the success rate for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return The success rate as a percentage
     */
    @Query("SELECT (COUNT(CASE WHEN r.success = true THEN 1 ELSE null END) * 100.0 / COUNT(r)) " +
            "FROM ApiConsumerOidRequestLog r WHERE r.oidMaster = :oidMaster")
    Double findSuccessRateByOidMaster(@Param("oidMaster") OidMaster oidMaster);

    /**
     * Find the average retry count for a specific OidMaster
     * 
     * @param oidMaster The OidMaster entity
     * @return The average retry count
     */
    @Query("SELECT AVG(r.retryCount) FROM ApiConsumerOidRequestLog r WHERE r.oidMaster = :oidMaster")
    Double findAverageRetryCountByOidMaster(@Param("oidMaster") OidMaster oidMaster);
}