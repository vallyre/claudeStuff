CREATE TABLE standard_codes_master (
                                            id SERIAL PRIMARY KEY,
                                            uuid UUID UNIQUE,
                                            url VARCHAR(255),
                                            version VARCHAR(50) NOT NULL,
                                            resource_type VARCHAR(50),
                                            name VARCHAR(255),
                                            title VARCHAR(255),
                                            status VARCHAR(50) NOT NULL,
                                            experimental BOOLEAN,
                                            date DATE,
                                            publisher VARCHAR(255),
                                            description TEXT,
                                            purpose TEXT,
                                            approval_date DATE,
                                            last_review_date DATE,
                                            effective_start_date DATE,
                                            effective_end_date DATE,
                                            updated_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            created_by VARCHAR(255),
                                            created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            updated_by VARCHAR(255)
);

-- SQL Script to create Standard Codes Responses table
CREATE TABLE standard_codes_responses (
                                          id SERIAL PRIMARY KEY,
                                          standard_codes_id UUID REFERENCES standard_codes_master(uuid),
                                          uuid UUID UNIQUE,
                                          api_response JSONB NOT NULL,
                                          response_time_ms INTEGER,
                                          http_status_code INTEGER,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          version INTEGER DEFAULT 1,
                                          is_current BOOLEAN DEFAULT TRUE,
                                          effective_start_date DATE,
                                          effective_end_date DATE,
                                          updated_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          created_by VARCHAR(255),
                                          created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_by VARCHAR(255)
);

);