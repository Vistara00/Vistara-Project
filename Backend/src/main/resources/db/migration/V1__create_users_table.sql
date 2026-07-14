-- =====================================================
-- V1: Create users table
-- =====================================================

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

CREATE TABLE IF NOT EXISTS users (
                                     id                      BIGSERIAL PRIMARY KEY,
                                     email                   VARCHAR(255) NOT NULL UNIQUE,
    password                VARCHAR(255) NOT NULL,
    full_name               VARCHAR(255) NOT NULL,
    phone_number            VARCHAR(20) NOT NULL,
    national_id             VARCHAR(50) UNIQUE,
    role                    VARCHAR(20) NOT NULL DEFAULT 'TOURIST',
    is_active               BOOLEAN DEFAULT TRUE,
    is_verified             BOOLEAN DEFAULT FALSE,
    emergency_contact_name  VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    name_search_vector      TSVECTOR GENERATED ALWAYS AS (
                                                             to_tsvector('english', full_name || ' ' || COALESCE(email, ''))
    ) STORED,
    last_login              TIMESTAMP,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at              TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('TOURIST', 'PARK_RANGER', 'ADMIN'))
    );

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_users_role_active ON users(role, is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_users_name_search ON users USING GIN (name_search_vector);