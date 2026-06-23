-- =====================================================
-- V3: Create location_tracking table
-- =====================================================

CREATE TABLE IF NOT EXISTS location_tracking (
                                                 id               BIGSERIAL        PRIMARY KEY,
                                                 session_id       BIGINT           NOT NULL REFERENCES visitor_sessions(id) ON DELETE CASCADE,
                                                 latitude         DOUBLE PRECISION NOT NULL,
                                                 longitude        DOUBLE PRECISION NOT NULL,
                                                 location_point   GEOMETRY(POINT, 4326) GENERATED ALWAYS AS (
                                                                      ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
                                                                      ) STORED,
                                                 accuracy         FLOAT,
                                                 speed            FLOAT,
                                                 bearing          FLOAT,
                                                 altitude         DOUBLE PRECISION,
                                                 battery_level    INTEGER CHECK (battery_level >= 0 AND battery_level <= 100),
                                                 is_within_geofence BOOLEAN DEFAULT TRUE,
                                                 is_off_trail     BOOLEAN DEFAULT FALSE,
                                                 timestamp        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  location_tracking IS 'Historical GPS location data for visitor tracking';
COMMENT ON COLUMN location_tracking.location_point IS 'PostGIS geometry point automatically generated from latitude/longitude';
COMMENT ON COLUMN location_tracking.accuracy       IS 'GPS accuracy in meters - lower is better';
COMMENT ON COLUMN location_tracking.is_off_trail   IS 'Whether visitor is outside designated trails';

CREATE INDEX IF NOT EXISTS idx_location_tracking_session
    ON location_tracking(session_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_location_tracking_timestamp
    ON location_tracking(timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_location_tracking_point
    ON location_tracking USING GIST (location_point);