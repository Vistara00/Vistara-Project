-- =====================================================
-- V15: Add location_point column to emergency_alerts
-- =====================================================

-- Add the location_point column as a generated column
ALTER TABLE emergency_alerts
    ADD COLUMN IF NOT EXISTS location_point GEOMETRY(POINT, 4326)
    GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)) STORED;

-- Add spatial index for performance
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_location_point
    ON emergency_alerts USING GIST (location_point);

-- Add comment
COMMENT ON COLUMN emergency_alerts.location_point IS 'PostGIS geometry point generated from latitude/longitude';