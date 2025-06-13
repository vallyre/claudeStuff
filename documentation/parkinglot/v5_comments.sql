-- V5__Comments.sql
-- Add descriptive comments to tables and columns

-- Set search path
SET search_path TO "code-bridge";

-- Table comments
COMMENT ON TABLE hli_api_config IS 'Configuration table for HLI API parameters. GIT Secrects or Config Table: TBD';
COMMENT ON TABLE oid_master IS 'Master table for Object Identifiers with healthcare coding information and cache status';
COMMENT ON TABLE oid_hli_api_response IS 'Stores the actual backend HLI API response data';
COMMENT ON TABLE api_consumer_response IS 'This table stores CCMT API Consumers Response Structure';
COMMENT ON TABLE api_consumer_request_type IS 'Configuration for different types of consumer API requests';
COMMENT ON TABLE api_consumer_request_log IS 'Log of all consumer API requests for monitoring and analytics';
COMMENT ON TABLE api_consumer_oid_request_log IS 'Log of all backend HLI API requests made by the service';
COMMENT ON TABLE oid_batch_process_log IS 'Log of batch processing operations for OIDs';
COMMENT ON TABLE cache_statistics IS 'Hourly statistics for cache performance monitoring';

-- Column comments
COMMENT ON COLUMN oid_master.oid IS 'Object Identifier used to query the HLI API and it is UNIQUE across the systems';
COMMENT ON COLUMN oid_master.code_group_content_set IS 'Content set to which the code group belongs';
COMMENT ON COLUMN oid_master.code_group_content_set_version IS 'Version number of the content set';
COMMENT ON COLUMN oid_master.code_sub_type IS 'Subtype classification of the code';
COMMENT ON COLUMN oid_master.fhir_identifier IS 'FHIR standard identifier for this code';
COMMENT ON COLUMN oid_master.hl7_uri IS 'HL7 URI reference for the code';
COMMENT ON COLUMN oid_master.code IS 'The actual code value';
COMMENT ON COLUMN oid_master.group_name IS 'Name of the code group';
COMMENT ON COLUMN oid_master.code_group_revision_name IS 'Name of the code group revision';
COMMENT ON COLUMN oid_master.revision_start IS 'Start timestamp of the revision period';
COMMENT ON COLUMN oid_master.revision_end IS 'End timestamp of the revision period';
COMMENT ON COLUMN oid_master.backend_cache_key IS 'Key used for storing OID data in Redis backend cache';
COMMENT ON COLUMN oid_master.backend_is_cached IS 'Flag indicating if OID data is currently in Redis backend cache';
COMMENT ON COLUMN oid_master.backend_redis_cache_expiry IS 'Timestamp when the Redis backend cache for this OID will expire';
COMMENT ON COLUMN oid_master.consumer_is_cached IS 'Flag indicating if consumer-formatted response is in Redis cache';
COMMENT ON COLUMN oid_master.consumer_redis_cache_expiry IS 'Timestamp when the Redis consumer cache for this OID will expire';
COMMENT ON COLUMN oid_master.consumer_cache_hits IS 'Counter for number of consumer cache hits for this OID';
COMMENT ON COLUMN oid_master.status IS 'Current status of the OID (PENDING, ACTIVE, ERROR, etc.)';

COMMENT ON COLUMN oid_hli_api_response.api_response IS 'Complete JSON response from the HLI API';
COMMENT ON COLUMN oid_hli_api_response.response_time_ms IS 'Time taken to receive response from HLI API in milliseconds';
COMMENT ON COLUMN oid_hli_api_response.version IS 'Version number for tracking changes in API responses over time';
COMMENT ON COLUMN oid_hli_api_response.is_current IS 'Flag indicating if this is the current/latest version of the response';

COMMENT ON COLUMN api_consumer_request_type.type_name IS 'Unique name identifying the consumer request type';
COMMENT ON COLUMN api_consumer_request_type.requires_oid IS 'Flag indicating if this request type requires an OID parameter';
COMMENT ON COLUMN api_consumer_request_type.additional_params IS 'JSON schema of additional parameters beyond OID';
COMMENT ON COLUMN api_consumer_request_type.cache_ttl_seconds IS 'Time-to-live for cache entries of this request type in seconds';

COMMENT ON COLUMN api_consumer_request_log.request_id IS 'External identifier for the API request';
COMMENT ON COLUMN api_consumer_request_log.request_ip IS 'IP address of the client making the request';
COMMENT ON COLUMN api_consumer_request_log.cache_hit IS 'Flag indicating if response was served from cache';

COMMENT ON COLUMN api_consumer_oid_request_log.retry_count IS 'Number of retry attempts made for this API request';
COMMENT ON COLUMN api_consumer_oid_request_log.request_headers IS 'HTTP headers sent with the API request';
COMMENT ON COLUMN api_consumer_oid_request_log.request_params IS 'Parameters sent with the API request';
COMMENT ON COLUMN api_consumer_oid_request_log.success IS 'Flag indicating if the API call was successful';

COMMENT ON COLUMN oid_batch_process_log.batch_id IS 'Unique identifier for the batch process';
COMMENT ON COLUMN oid_batch_process_log.total_oids IS 'Total number of OIDs in the batch';
COMMENT ON COLUMN oid_batch_process_log.successful_oids IS 'Number of OIDs successfully processed in the batch';
COMMENT ON COLUMN oid_batch_process_log.failed_oids IS 'Number of OIDs that failed processing in the batch';

COMMENT ON COLUMN cache_statistics.backend_cache_hit_rate IS 'Percentage of backend cache hits relative to total requests';
COMMENT ON COLUMN cache_statistics.consumer_cache_hit_rate IS 'Percentage of consumer cache hits relative to total requests';
COMMENT ON COLUMN cache_statistics.redis_memory_usage_mb IS 'Redis memory usage in megabytes for monitoring purposes';
COMMENT ON COLUMN cache_statistics.p95_response_time_ms IS '95th percentile response time in milliseconds';
COMMENT ON COLUMN cache_statistics.p99_response_time_ms IS '99th percentile response time in milliseconds';

-- View comments
COMMENT ON VIEW v_oid_complete_status IS 'Comprehensive view of OID status including backend and consumer cache information';
COMMENT ON VIEW v_cache_performance IS 'Performance metrics for consumer API cache by request type';
COMMENT ON VIEW v_batch_processing_stats IS 'Statistics and performance metrics for batch processing operations';
COMMENT ON VIEW v_api_health_status IS 'Hourly health metrics for the HLI API integration';
