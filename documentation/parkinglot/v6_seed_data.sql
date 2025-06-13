-- V6__Seed_Data.sql
-- Insert initial seed data for configuration tables

-- Set search path
SET search_path TO "code-bridge";

-- Insert default HLI API configuration
INSERT INTO hli_api_config (
    config_name,
    retry_limit,
    retry_interval_ms,
    max_response_time_ms,
    batch_size,
    api_base_url,
    timeout_ms,
    created_by,
    is_active
) VALUES (
    'default_usoncology_config',
    3,
    5000,
    10000,
    100,
    'https://usoncology.healthlanguage.com',
    30000,
    'system',
    TRUE
);

-- Insert default consumer request type
INSERT INTO api_consumer_request_type (
    type_name,
    description,
    requires_oid,
    cache_ttl_seconds,
    is_active
) VALUES (
    'standard_oid_lookup',
    'Standard OID lookup request from consumers',
    TRUE,
    3600,
    TRUE
);

-- Insert advanced search consumer request type
INSERT INTO api_consumer_request_type (
    type_name,
    description,
    requires_oid,
    additional_params,
    cache_ttl_seconds,
    is_active
) VALUES (
    'advanced_oid_search',
    'Advanced search with additional parameters beyond OID',
    FALSE,
    '{"allowable_params": ["code", "group_name", "fhir_identifier", "content_set"]}',
    1800,
    TRUE
);

-- Insert batch processing consumer request type
INSERT INTO api_consumer_request_type (
    type_name,
    description,
    requires_oid,
    additional_params,
    cache_ttl_seconds,
    is_active
) VALUES (
    'batch_oid_request',
    'Process multiple OIDs in a single request',
    FALSE,
    '{"allowable_params": ["oid_list", "response_format"]}',
    7200,
    TRUE
);
