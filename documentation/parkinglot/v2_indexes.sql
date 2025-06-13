-- V2__Indexes.sql
-- Create indexes for better query performance

-- Set search path
SET search_path TO "code-bridge";

-- hli_api_config indexes
CREATE INDEX idx_hli_api_config_name ON hli_api_config(config_name);
CREATE INDEX idx_hli_api_config_is_active ON hli_api_config(is_active);

-- oid_master indexes
CREATE INDEX idx_oid_master_oid ON oid_master(oid);
CREATE INDEX idx_oid_master_code ON oid_master(code);
CREATE INDEX idx_oid_master_fhir_identifier ON oid_master(fhir_identifier);
CREATE INDEX idx_oid_master_group_name ON oid_master(group_name);
CREATE INDEX idx_oid_master_code_group_content_set ON oid_master(code_group_content_set);
CREATE INDEX idx_oid_master_revision_period ON oid_master(revision_start, revision_end);
CREATE INDEX idx_oid_master_backend_cache_key ON oid_master(backend_cache_key);
CREATE INDEX idx_oid_master_backend_is_cached ON oid_master(backend_is_cached);
CREATE INDEX idx_oid_master_consumer_is_cached ON oid_master(consumer_is_cached);
CREATE INDEX idx_oid_master_status ON oid_master(status);
CREATE INDEX idx_oid_master_hli_api_config_id ON oid_master(hli_api_config_id);
CREATE INDEX idx_oid_master_is_active ON oid_master(is_active);

-- oid_hli_api_response indexes
CREATE INDEX idx_oid_hli_api_response_oid_master_id ON oid_hli_api_response(oid_master_id);
CREATE INDEX idx_oid_hli_api_response_is_current ON oid_hli_api_response(is_current);
CREATE INDEX idx_oid_hli_api_response_http_status_code ON oid_hli_api_response(http_status_code);

-- api_consumer_response indexes
CREATE INDEX idx_api_consumer_response_oid_master_id ON api_consumer_response(oid_master_id);
CREATE INDEX idx_api_consumer_response_is_current ON api_consumer_response(is_current);

-- api_consumer_request_log indexes
CREATE INDEX idx_api_consumer_request_log_request_timestamp ON api_consumer_request_log(request_timestamp);
CREATE INDEX idx_api_consumer_request_log_cache_hit ON api_consumer_request_log(cache_hit);
CREATE INDEX idx_api_consumer_request_log_oid_master_id ON api_consumer_request_log(oid_master_id);
CREATE INDEX idx_api_consumer_request_log_request_type_id ON api_consumer_request_log(request_type_id);
CREATE INDEX idx_api_consumer_request_log_request_id ON api_consumer_request_log(request_id);
CREATE INDEX idx_api_consumer_request_log_http_status_code ON api_consumer_request_log(http_status_code);

-- api_consumer_oid_request_log indexes
CREATE INDEX idx_api_consumer_oid_request_log_oid_master_id ON api_consumer_oid_request_log(oid_master_id);
CREATE INDEX idx_api_consumer_oid_request_log_success ON api_consumer_oid_request_log(success);
CREATE INDEX idx_api_consumer_oid_request_log_request_timestamp ON api_consumer_oid_request_log(request_timestamp);
CREATE INDEX idx_api_consumer_oid_request_log_http_status_code ON api_consumer_oid_request_log(http_status_code);

-- oid_batch_process_log indexes
CREATE INDEX idx_oid_batch_process_log_batch_id ON oid_batch_process_log(batch_id);
CREATE INDEX idx_oid_batch_process_log_status ON oid_batch_process_log(status);
CREATE INDEX idx_oid_batch_process_log_hli_api_config_id ON oid_batch_process_log(hli_api_config_id);

-- cache_statistics indexes
CREATE INDEX idx_cache_statistics_date ON cache_statistics(date);
CREATE INDEX idx_cache_statistics_date_hour ON cache_statistics(date, hour);
