-- V3__Triggers.sql
-- Create triggers for automatic timestamp updates and cache hit tracking

-- Set search path
SET search_path TO "code-bridge";

-- Function to update timestamp when record is updated
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers to automatically update the updated_at timestamp
CREATE TRIGGER update_oid_master_timestamp
BEFORE UPDATE ON oid_master
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_hli_api_config_timestamp
BEFORE UPDATE ON hli_api_config
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_api_consumer_request_type_timestamp
BEFORE UPDATE ON api_consumer_request_type
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_api_consumer_response_timestamp
BEFORE UPDATE ON api_consumer_response
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- Function to update consumer cache hit count
CREATE OR REPLACE FUNCTION increment_consumer_cache_hit()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.cache_hit = TRUE THEN
        UPDATE oid_master
        SET consumer_cache_hits = consumer_cache_hits + 1
        WHERE id = NEW.oid_master_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to increment consumer cache hit count
CREATE TRIGGER update_consumer_cache_hit_count
AFTER INSERT ON api_consumer_request_log
FOR EACH ROW
EXECUTE FUNCTION increment_consumer_cache_hit();
