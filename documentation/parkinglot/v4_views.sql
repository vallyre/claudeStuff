-- V4__Views.sql
-- Create views for easier querying and monitoring

-- Set search path
SET search_path TO "code-bridge";

-- View to get comprehensive OID status with both backend and consumer cache info
CREATE VIEW v_oid_complete_status AS
SELECT 
    m.id, 
    m.oid, 
    m.code_group_content_set,
    m.code_group_content_set_version,
    m.code_sub_type,
    m.fhir_identifier,
    m.hl7_uri,
    m.code,
    m.group_name,
    m.code_group_revision_name,
    m.revision_start,
    m.revision_end,
    m.backend_is_cached,
    m.backend_redis_cache_expiry,
    m.consumer_is_cached,
    m.consumer_redis_cache_expiry,
    m.consumer_cache_hits,
    m.status,
    m.is_active,
    m.last_api_call,
    m.api_call_count,
    c.config_name,
    c.retry_limit,
    c.retry_interval_ms,
    c.max_response_time_ms,
    r.api_response,
    cr.consumer_response,
    r.http_status_code as backend_http_status,
    r.response_time_ms as backend_response_time
FROM 
    oid_master m
JOIN 
    hli_api_config c ON m.hli_api_config_id = c.id
LEFT JOIN 
    oid_hli_api_response r ON m.id = r.oid_master_id AND r.is_current = TRUE
LEFT JOIN
    api_consumer_response cr ON m.id = cr.oid_master_id AND cr.is_current = TRUE;

-- View for cache performance metrics
CREATE VIEW v_cache_performance AS
SELECT
    t.type_name as request_type,
    COUNT(l.id) AS total_requests,
    SUM(CASE WHEN l.cache_hit THEN 1 ELSE 0 END) AS cache_hits,
    SUM(CASE WHEN NOT l.cache_hit THEN 1 ELSE 0 END) AS cache_misses,
    CASE 
        WHEN COUNT(l.id) > 0 
        THEN ROUND((SUM(CASE WHEN l.cache_hit THEN 1 ELSE 0 END)::numeric / COUNT(l.id)::numeric) * 100, 2)
        ELSE 0 
    END AS hit_rate_percentage,
    AVG(CASE WHEN l.cache_hit THEN l.response_time_ms ELSE NULL END) AS avg_cached_response_time,
    AVG(CASE WHEN NOT l.cache_hit THEN l.response_time_ms ELSE NULL END) AS avg_uncached_response_time,
    MIN(l.request_timestamp) AS first_request,
    MAX(l.request_timestamp) AS last_request
FROM
    api_consumer_request_log l
JOIN
    api_consumer_request_type t ON l.request_type_id = t.id
GROUP BY
    t.type_name;

-- View for tracking batch processing performance
CREATE VIEW v_batch_processing_stats AS
SELECT
    b.batch_id,
    c.config_name,
    b.total_oids,
    b.successful_oids,
    b.failed_oids,
    CASE 
        WHEN b.total_oids > 0 
        THEN ROUND((b.successful_oids::numeric / b.total_oids::numeric) * 100, 2)
        ELSE 0 
    END AS success_rate_percentage,
    b.batch_start_time,
    b.batch_end_time,
    EXTRACT(EPOCH FROM (b.batch_end_time - b.batch_start_time)) AS duration_seconds,
    b.status,
    b.error_message
FROM
    oid_batch_process_log b
JOIN
    hli_api_config c ON b.hli_api_config_id = c.id;

-- View for API health monitoring
CREATE VIEW v_api_health_status AS
SELECT
    DATE_TRUNC('hour', l.request_timestamp) AS hour,
    COUNT(*) AS total_requests,
    SUM(CASE WHEN l.success THEN 1 ELSE 0 END) AS successful_requests,
    SUM(CASE WHEN NOT l.success THEN 1 ELSE 0 END) AS failed_requests,
    CASE 
        WHEN COUNT(*) > 0 
        THEN ROUND((SUM(CASE WHEN l.success THEN 1 ELSE 0 END)::numeric / COUNT(*)::numeric) * 100, 2)
        ELSE 0 
    END AS success_rate_percentage,
    AVG(l.response_time_ms) AS avg_response_time,
    MAX(l.response_time_ms) AS max_response_time,
    MIN(l.response_time_ms) AS min_response_time,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY l.response_time_ms) AS p95_response_time,
    MAX(l.retry_count) AS max_retries,
    AVG(l.retry_count) AS avg_retries
FROM
    api_consumer_oid_request_log l
GROUP BY
    DATE_TRUNC('hour', l.request_timestamp)
ORDER BY
    hour DESC;
