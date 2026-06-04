-- =====================================================
-- V3: Create location_tracking table
-- Description: Stores historical GPS location data for visitors
-- =====================================================

CREATE TABLE IF NOT EXISTS location_tracking (
    id               BIGSERIAL        PRIMARY KEY,
    session_id       BIGINT           NOT NULL REFERENCES visitor_sessions(id) ON DELETE CASCADE,
    latitude         DOUBLE PRECISION NOT NULL,
    longitude        DOUBLE PRECISION NOT NULL,
    location_point   GEOMETRY(POINT, 4326) GENERATED ALWAYS AS (
                         ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
                     ) STORED,
    accuracy         FLOAT,            -- GPS accuracy in meters
    speed            FLOAT,            -- Speed in km/h
    bearing          FLOAT,            -- Direction in degrees
    altitude         DOUBLE PRECISION,
    battery_level    INTEGER CHECK (battery_level >= 0 AND battery_level <= 100),
    is_within_geofence BOOLEAN DEFAULT TRUE,
    is_off_trail     BOOLEAN DEFAULT FALSE,
    timestamp        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    -- FIX (Bug 3): Removed the volatile CHECK constraint on timestamp.
    -- CURRENT_TIMESTAMP in a CHECK evaluates at transaction start and is
    -- unreliable in batch inserts. Enforce recency at the application layer.
);

-- Add comments
COMMENT ON TABLE  location_tracking IS 'Historical GPS location data for visitor tracking';
COMMENT ON COLUMN location_tracking.location_point IS 'PostGIS geometry point automatically generated from latitude/longitude';
COMMENT ON COLUMN location_tracking.accuracy       IS 'GPS accuracy in meters - lower is better';
COMMENT ON COLUMN location_tracking.is_off_trail   IS 'Whether visitor is outside designated trails';

-- Index for session + time queries (covers most access patterns)
CREATE INDEX IF NOT EXISTS idx_location_tracking_session
    ON location_tracking(session_id, timestamp DESC);

-- Index for global time-range queries
CREATE INDEX IF NOT EXISTS idx_location_tracking_timestamp
    ON location_tracking(timestamp DESC);

-- Spatial index for geospatial queries
CREATE INDEX IF NOT EXISTS idx_location_tracking_point
    ON location_tracking USING GIST (location_point);

-- FIX (Bug 2): Removed the volatile partial index:
--   WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '24 hours'
-- PostgreSQL requires index predicates to be immutable. CURRENT_TIMESTAMP is
-- volatile, so the predicate becomes stale instantly and the index is useless.
-- Recent-record queries are served efficiently by idx_location_tracking_session
-- with an explicit WHERE clause in the query itself.

-- Partition comment (uncomment if millions of rows are expected):
/*
CREATE TABLE location_tracking_2024_01 PARTITION OF location_tracking
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
*/
