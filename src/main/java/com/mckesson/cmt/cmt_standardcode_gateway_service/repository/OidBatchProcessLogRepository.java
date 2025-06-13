package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.HliApiConfig;
import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.OidBatchProcessLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OidBatchProcessLog entities that provides CRUD operations
 */
@Repository
public interface OidBatchProcessLogRepository extends JpaRepository<OidBatchProcessLog, Integer> {

    /**
     * Find an OidBatchProcessLog by its batch ID
     * 
     * @param batchId The batch ID to search for
     * @return An Optional containing the OidBatchProcessLog if found
     */
    Optional<OidBatchProcessLog> findByBatchId(String batchId);

    /**
     * Find all OidBatchProcessLog records for a specific HliApiConfig
     * 
     * @param hliApiConfig The HliApiConfig entity
     * @return A list of matching OidBatchProcessLog records
     */
    List<OidBatchProcessLog> findByHliApiConfig(HliApiConfig hliApiConfig);

    /**
     * Find all OidBatchProcessLog records with a specific status
     * 
     * @param status The status to search for
     * @return A list of matching OidBatchProcessLog records
     */
    List<OidBatchProcessLog> findByStatus(String status);

    /**
     * Find all OidBatchProcessLog records within a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return A list of matching OidBatchProcessLog records
     */
    List<OidBatchProcessLog> findByBatchStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all OidBatchProcessLog records for a specific HliApiConfig ordered by
     * batch start time descending
     * 
     * @param hliApiConfig The HliApiConfig entity
     * @return A list of matching OidBatchProcessLog records
     */
    List<OidBatchProcessLog> findByHliApiConfigOrderByBatchStartTimeDesc(HliApiConfig hliApiConfig);

    /**
     * Find the most recent batch process for a specific HliApiConfig
     * 
     * @param hliApiConfig The HliApiConfig entity
     * @return An Optional containing the most recent OidBatchProcessLog if found
     */
    Optional<OidBatchProcessLog> findFirstByHliApiConfigOrderByBatchStartTimeDesc(HliApiConfig hliApiConfig);

    /**
     * Find the success rate for batches by HliApiConfig
     * 
     * @param hliApiConfig The HliApiConfig entity
     * @return The average success rate as a percentage
     */
    @Query("SELECT AVG((b.successfulOids * 100.0) / b.totalOids) FROM OidBatchProcessLog b " +
            "WHERE b.hliApiConfig = :hliApiConfig AND b.status = 'COMPLETED'")
    Double findAverageSuccessRateByHliApiConfig(@Param("hliApiConfig") HliApiConfig hliApiConfig);

    /**
     * Find the average processing time for batches by HliApiConfig
     * 
     * @param hliApiConfig The HliApiConfig entity
     * @return The average processing time in milliseconds
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', SECOND, b.batchStartTime, b.batchEndTime)) " +
            "FROM OidBatchProcessLog b WHERE b.hliApiConfig = :hliApiConfig AND b.status = 'COMPLETED'")
    Double findAverageProcessingTimeByHliApiConfig(@Param("hliApiConfig") HliApiConfig hliApiConfig);

    /**
     * Count the number of batches by status for a specific HliApiConfig
     * 
     * @param hliApiConfig The HliApiConfig entity
     * @param status       The status to count
     * @return The count of matching batches
     */
    Long countByHliApiConfigAndStatus(HliApiConfig hliApiConfig, String status);
}