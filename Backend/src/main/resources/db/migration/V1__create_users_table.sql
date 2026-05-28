-- =====================================================
-- V1: Create users table for Vistara System
-- Description: Stores all user information (tourists, rangers, admins)
-- =====================================================

-- Enable PostGIS extension if not already enabled
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- Create ENUM types
DO $$ BEGIN
CREATE TYPE user_role AS ENUM ('TOURIST', 'PARK_RANGER', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
                                     id                      BIGSERIAL PRIMARY KEY,
                                     email                   VARCHAR(255) NOT NULL UNIQUE,
    password                VARCHAR(255) NOT NULL,
    full_name               VARCHAR(255) NOT NULL,
    phone_number            VARCHAR(20) NOT NULL,
    national_id             VARCHAR(50) UNIQUE,
    role                    user_role NOT NULL DEFAULT 'TOURIST',
    is_active               BOOLEAN DEFAULT TRUE,
    is_verified             BOOLEAN DEFAULT FALSE,
    emergency_contact_name  VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    last_login              TIMESTAMP,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at              TIMESTAMP
    );

-- Add comments for documentation
COMMENT ON TABLE users IS 'Stores all system users including tourists, park rangers, and administrators';
COMMENT ON COLUMN users.email IS 'User email address - used for login';
COMMENT ON COLUMN users.role IS 'User role: TOURIST, PARK_RANGER, or ADMIN';
COMMENT ON COLUMN users.emergency_contact_name IS 'Name of person to contact in emergency';
COMMENT ON COLUMN users.emergency_contact_phone IS 'Phone number of emergency contact';

-- Create trigger to automatically update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();