package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.CacheStatistics;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for CacheStatistics entities that provides CRUD operations
 */
@Repository
public interface CacheStatisticsRepository extends JpaRepository<CacheStatistics, Integer> {

    /**
     * Find all CacheStatistics records for a specific date
     * 
     * @param date The date to search for
     * @return A list of matching CacheStatistics records
     */
    List<CacheStatistics> findByDate(LocalDate date);

    /**
     * Find all CacheStatistics records for a specific date and hour
     * 
     * @param date The date to search for
     * @param hour The hour to search for (0-23)
     * @return A list of matching CacheStatistics records
     */
    List<CacheStatistics> findByDateAndHour(LocalDate date, Integer hour);

    /**
     * Find all CacheStatistics records for a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return A list of matching CacheStatistics records
     */
    List<CacheStatistics> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find all CacheStatistics records ordered by date and hour descending
     * 
     * @return A list of CacheStatistics records
     */
    List<CacheStatistics> findAllByOrderByDateDescHourDesc();

    /**
     * Calculate the average backend cache hit rate for a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return The average backend cache hit rate
     */
    @Query("SELECT AVG(c.backendCacheHitRate) FROM CacheStatistics c WHERE c.date BETWEEN :startDate AND :endDate")
    Double calculateAverageBackendCacheHitRate(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate the average consumer cache hit rate for a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return The average consumer cache hit rate
     */
    @Query("SELECT AVG(c.consumerCacheHitRate) FROM CacheStatistics c WHERE c.date BETWEEN :startDate AND :endDate")
    Double calculateAverageConsumerCacheHitRate(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate the average Redis memory usage for a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return The average Redis memory usage in MB
     */
    @Query("SELECT AVG(c.redisMemoryUsageMb) FROM CacheStatistics c WHERE c.date BETWEEN :startDate AND :endDate")
    Double calculateAverageRedisMemoryUsage(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find the maximum p95 response time for a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return The maximum p95 response time in milliseconds
     */
    @Query("SELECT MAX(c.p95ResponseTimeMs) FROM CacheStatistics c WHERE c.date BETWEEN :startDate AND :endDate")
    Integer findMaxP95ResponseTime(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find the maximum p99 response time for a date range
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return The maximum p99 response time in milliseconds
     */
    @Query("SELECT MAX(c.p99ResponseTimeMs) FROM CacheStatistics c WHERE c.date BETWEEN :startDate AND :endDate")
    Integer findMaxP99ResponseTime(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Calculate the response time improvement from caching
     * 
     * @param date The date to calculate for
     * @return The average percentage improvement
     */
    @Query("SELECT AVG(((c.avgUncachedResponseTimeMs - c.avgCachedResponseTimeMs) * 100.0) / c.avgUncachedResponseTimeMs) "
            +
            "FROM CacheStatistics c WHERE c.date = :date AND c.avgUncachedResponseTimeMs > 0")
    Double calculateCachingImprovementPercentage(@Param("date") LocalDate date);
}