-- =====================================================
-- V1: Create users table for Vistara System
-- =====================================================

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

CREATE TABLE IF NOT EXISTS users (
                                     id                      BIGSERIAL    PRIMARY KEY,
                                     email                   VARCHAR(255) NOT NULL UNIQUE,
    password                VARCHAR(255) NOT NULL,
    full_name               VARCHAR(255) NOT NULL,
    phone_number            VARCHAR(20)  NOT NULL,
    national_id             VARCHAR(50)  UNIQUE,
    role                    VARCHAR(20)  NOT NULL DEFAULT 'TOURIST',
    is_active               BOOLEAN      DEFAULT TRUE,
    is_verified             BOOLEAN      DEFAULT FALSE,
    emergency_contact_name  VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    name_search_vector      TSVECTOR GENERATED ALWAYS AS (
                                                             to_tsvector('english', full_name || ' ' || COALESCE(email, ''))
    ) STORED,
    last_login              TIMESTAMP,
    created_at              TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    deleted_at              TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('TOURIST', 'PARK_RANGER', 'ADMIN'))
    );

COMMENT ON TABLE  users IS 'Stores all system users including tourists, park rangers, and administrators';
COMMENT ON COLUMN users.email IS 'User email address - used for login';
COMMENT ON COLUMN users.role  IS 'User role: TOURIST, PARK_RANGER, or ADMIN';
COMMENT ON COLUMN users.emergency_contact_name  IS 'Name of person to contact in emergency';
COMMENT ON COLUMN users.emergency_contact_phone IS 'Phone number of emergency contact';
COMMENT ON COLUMN users.name_search_vector IS 'Generated tsvector for full-text search on name and email';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active) WHERE is_active = TRUE;