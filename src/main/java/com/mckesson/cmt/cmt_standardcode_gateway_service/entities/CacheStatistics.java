package com.mckesson.cmt.cmt_standardcode_gateway_service.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cache_statistics")
public class CacheStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "hour")
    private Integer hour;

    @Column(name = "backend_cache_hits")
    private Integer backendCacheHits;

    @Column(name = "backend_cache_misses")
    private Integer backendCacheMisses;

    @Column(name = "backend_cache_hit_rate")
    private BigDecimal backendCacheHitRate;

    @Column(name = "consumer_cache_hits")
    private Integer consumerCacheHits;

    @Column(name = "consumer_cache_misses")
    private Integer consumerCacheMisses;

    @Column(name = "consumer_cache_hit_rate")
    private BigDecimal consumerCacheHitRate;

    @Column(name = "redis_memory_usage_mb")
    private BigDecimal redisMemoryUsageMb;

    @Column(name = "avg_cached_response_time_ms")
    private Integer avgCachedResponseTimeMs;

    @Column(name = "avg_uncached_response_time_ms")
    private Integer avgUncachedResponseTimeMs;

    @Column(name = "p95_response_time_ms")
    private Integer p95ResponseTimeMs;

    @Column(name = "p99_response_time_ms")
    private Integer p99ResponseTimeMs;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getBackendCacheHits() {
        return backendCacheHits;
    }

    public void setBackendCacheHits(Integer backendCacheHits) {
        this.backendCacheHits = backendCacheHits;
    }

    public Integer getBackendCacheMisses() {
        return backendCacheMisses;
    }

    public void setBackendCacheMisses(Integer backendCacheMisses) {
        this.backendCacheMisses = backendCacheMisses;
    }

    public BigDecimal getBackendCacheHitRate() {
        return backendCacheHitRate;
    }

    public void setBackendCacheHitRate(BigDecimal backendCacheHitRate) {
        this.backendCacheHitRate = backendCacheHitRate;
    }

    public Integer getConsumerCacheHits() {
        return consumerCacheHits;
    }

    public void setConsumerCacheHits(Integer consumerCacheHits) {
        this.consumerCacheHits = consumerCacheHits;
    }

    public Integer getConsumerCacheMisses() {
        return consumerCacheMisses;
    }

    public void setConsumerCacheMisses(Integer consumerCacheMisses) {
        this.consumerCacheMisses = consumerCacheMisses;
    }

    public BigDecimal getConsumerCacheHitRate() {
        return consumerCacheHitRate;
    }

    public void setConsumerCacheHitRate(BigDecimal consumerCacheHitRate) {
        this.consumerCacheHitRate = consumerCacheHitRate;
    }

    public BigDecimal getRedisMemoryUsageMb() {
        return redisMemoryUsageMb;
    }

    public void setRedisMemoryUsageMb(BigDecimal redisMemoryUsageMb) {
        this.redisMemoryUsageMb = redisMemoryUsageMb;
    }

    public Integer getAvgCachedResponseTimeMs() {
        return avgCachedResponseTimeMs;
    }

    public void setAvgCachedResponseTimeMs(Integer avgCachedResponseTimeMs) {
        this.avgCachedResponseTimeMs = avgCachedResponseTimeMs;
    }

    public Integer getAvgUncachedResponseTimeMs() {
        return avgUncachedResponseTimeMs;
    }

    public void setAvgUncachedResponseTimeMs(Integer avgUncachedResponseTimeMs) {
        this.avgUncachedResponseTimeMs = avgUncachedResponseTimeMs;
    }

    public Integer getP95ResponseTimeMs() {
        return p95ResponseTimeMs;
    }

    public void setP95ResponseTimeMs(Integer p95ResponseTimeMs) {
        this.p95ResponseTimeMs = p95ResponseTimeMs;
    }

    public Integer getP99ResponseTimeMs() {
        return p99ResponseTimeMs;
    }

    public void setP99ResponseTimeMs(Integer p99ResponseTimeMs) {
        this.p99ResponseTimeMs = p99ResponseTimeMs;
    }
}
