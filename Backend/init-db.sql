-- init-db.sql
-- This file runs when the PostgreSQL container starts

-- Create extensions
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- Create additional schemas if needed
-- GRANT ALL PRIVILEGES ON DATABASE vistara_db TO postgres;