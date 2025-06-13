-- V1__Initial_Schema.sql
-- Initial schema creation for Health Language API Caching System

-- Create schema
CREATE SCHEMA IF NOT EXISTS "code-bridge";

-- Set search path
SET search_path TO "code-bridge";

-- HLI API Configuration Table (independent of OIDs)
CREATE TABLE hli_api_config (
    id SERIAL PRIMARY KEY,
    config_name VARCHAR(100) NOT NULL UNIQUE,
    retry_limit INTEGER DEFAULT 3,
    retry_interval_ms INTEGER DEFAULT 5000,
    max_response_time_ms INTEGER DEFAULT 10000,
    batch_size INTEGER DEFAULT 100,
    api_base_url VARCHAR(255) DEFAULT 'https://usoncology.healthlanguage.com',
    timeout_ms INTEGER DEFAULT 30000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE
);

-- OID Master Table (lightweight main table)
CREATE TABLE oid_master (
    id SERIAL PRIMARY KEY,
    oid VARCHAR(255) NOT NULL UNIQUE,
    
    -- Healthcare terminology fields
    code_group_content_set VARCHAR(255),
    code_group_content_set_version VARCHAR(100),
    code_sub_type VARCHAR(100),
    fhir_identifier VARCHAR(255),
    hl7_uri VARCHAR(255),
    code VARCHAR(100),
    group_name VARCHAR(255),
    code_group_revision_name VARCHAR(255),
    revision_start TIMESTAMP,
    revision_end TIMESTAMP,
    
    -- Cache tracking fields for backend API
    backend_cache_key VARCHAR(255),
    backend_is_cached BOOLEAN DEFAULT FALSE,
    backend_redis_cache_expiry TIMESTAMP,
    
    -- Cache tracking fields for consumer API
    consumer_is_cached BOOLEAN DEFAULT FALSE,
    consumer_redis_cache_expiry TIMESTAMP,
    consumer_cache_hits INTEGER DEFAULT 0,
    
    -- Reference to configuration
    hli_api_config_id INTEGER NOT NULL REFERENCES hli_api_config(id),
    
    -- Status fields
    is_active BOOLEAN DEFAULT TRUE,
    status VARCHAR(50) DEFAULT 'PENDING',
    last_status_update TIMESTAMP,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_api_call TIMESTAMP,
    api_call_count INTEGER DEFAULT 0
);

-- HLI API Response Table (stores the actual backend API response data)
CREATE TABLE oid_hli_api_response (
    id SERIAL PRIMARY KEY,
    oid_master_id INTEGER NOT NULL REFERENCES oid_master(id),
    api_response JSONB NOT NULL,
    response_time_ms INTEGER,
    http_status_code INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Versioning support (for historical responses)
    version INTEGER DEFAULT 1,
    is_current BOOLEAN DEFAULT TRUE
);

-- Consumer API Response Table (stores the transformed response for consumer APIs)
CREATE TABLE api_consumer_response (
    id SERIAL PRIMARY KEY,
    oid_master_id INTEGER NOT NULL REFERENCES oid_master(id),
    consumer_response JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Versioning support (for historical responses)
    version INTEGER DEFAULT 1,
    is_current BOOLEAN DEFAULT TRUE
);

-- Consumer API Request Types (simpler patterns table)
CREATE TABLE api_consumer_request_type (
    id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    requires_oid BOOLEAN DEFAULT TRUE,
    additional_params JSONB,
    cache_ttl_seconds INTEGER DEFAULT 3600,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Consumer API Request Log
CREATE TABLE api_consumer_request_log (
    id SERIAL PRIMARY KEY,
    request_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response_timestamp TIMESTAMP,
    request_type_id INTEGER REFERENCES api_consumer_request_type(id),
    request_id VARCHAR(255),
    oid_master_id INTEGER REFERENCES oid_master(id),
    request_ip VARCHAR(45),
    additional_params JSONB,
    http_status_code INTEGER,
    response_time_ms INTEGER,
    cache_hit BOOLEAN DEFAULT FALSE,
    error_message TEXT
);

-- HLI API Request Log (for backend API calls)
CREATE TABLE api_consumer_oid_request_log (
    id SERIAL PRIMARY KEY,
    oid_master_id INTEGER NOT NULL REFERENCES oid_master(id),
    request_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response_timestamp TIMESTAMP,
    response_time_ms INTEGER,
    http_status_code INTEGER,
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    request_headers JSONB,
    request_params JSONB,
    success BOOLEAN
);

-- Batch Processing Log
CREATE TABLE oid_batch_process_log (
    id SERIAL PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    hli_api_config_id INTEGER REFERENCES hli_api_config(id),
    batch_start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    batch_end_time TIMESTAMP,
    total_oids INTEGER,
    successful_oids INTEGER DEFAULT 0,
    failed_oids INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'RUNNING',
    error_message TEXT
);

-- Cache Statistics Table (for monitoring cache effectiveness)
CREATE TABLE cache_statistics (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    hour INTEGER NOT NULL,
    
    -- Backend API cache stats
    backend_cache_hits INTEGER DEFAULT 0,
    backend_cache_misses INTEGER DEFAULT 0,
    backend_cache_hit_rate NUMERIC(5,2),
    
    -- Consumer API cache stats
    consumer_cache_hits INTEGER DEFAULT 0,
    consumer_cache_misses INTEGER DEFAULT 0,
    consumer_cache_hit_rate NUMERIC(5,2),
    
    -- Redis memory usage
    redis_memory_usage_mb NUMERIC(10,2),
    
    -- Response time metrics (milliseconds)
    avg_cached_response_time_ms INTEGER,
    avg_uncached_response_time_ms INTEGER,
    p95_response_time_ms INTEGER,
    p99_response_time_ms INTEGER,
    
    UNIQUE(date, hour)
);


ALTER TABLE "code-bridge".oid_master 
ADD COLUMN service_method_name VARCHAR(255);


-- SQL script to create and populate scheduler_config table

-- Create scheduler_config table
CREATE TABLE scheduler_config (
    id SERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL UNIQUE,
    cron_expression VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    description TEXT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(100)
);

-- Add comments to table and columns
COMMENT ON TABLE scheduler_config IS 'Stores configuration for scheduled jobs';
COMMENT ON COLUMN scheduler_config.job_name IS 'Unique identifier for the job';
COMMENT ON COLUMN scheduler_config.cron_expression IS 'Cron expression that determines when the job runs';
COMMENT ON COLUMN scheduler_config.enabled IS 'Flag indicating whether the job is active';
COMMENT ON COLUMN scheduler_config.description IS 'Description of what the job does';
COMMENT ON COLUMN scheduler_config.last_updated IS 'Timestamp of when the configuration was last modified';
COMMENT ON COLUMN scheduler_config.last_updated_by IS 'User who last modified the configuration';

-- Create index on job_name for faster lookups
CREATE INDEX idx_scheduler_config_job_name ON scheduler_config(job_name);

-- Insert default OID processing job configuration
INSERT INTO scheduler_config (
    job_name, 
    cron_expression, 
    enabled, 
    description, 
    last_updated_by
)
VALUES (
    'oidProcessing', 
    '0 0 2 * * ?', -- Runs at 2:00 AM every day
    true, 
    'Daily batch processing job for OIDs that need updating', 
    'system'
);

-- Example: Insert configuration for another job (uncomment if needed)
/*
INSERT INTO scheduler_config (
    job_name, 
    cron_expression, 
    enabled, 
    description, 
    last_updated_by
)
VALUES (
    'cacheCleanup', 
    '0 0 * * * ?', -- Runs every hour
    true, 
    'Hourly job to clean up expired cache entries', 
    'system'
);
*/

-- Grant permissions (adjust as needed for your environment)
GRANT SELECT, INSERT, UPDATE ON scheduler_config TO cmt_user, cmt_admin, cmt_read_only;
GRANT USAGE, SELECT ON SEQUENCE scheduler_config_id_seq TO cmt_user, cmt_admin, cmt_read_only;