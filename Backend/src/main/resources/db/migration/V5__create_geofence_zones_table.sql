-- =====================================================
-- V5: Create geofence_zones table
-- Description: Defines safe and restricted zones within the park
-- =====================================================

DO $$ BEGIN
CREATE TYPE zone_type AS ENUM ('SAFE', 'RESTRICTED', 'DANGER', 'WILDLIFE_AREA', 'EMERGENCY_EXIT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS geofence_zones (
                                              id                      BIGSERIAL PRIMARY KEY,
                                              zone_name               VARCHAR(255) NOT NULL,
    zone_description        TEXT,
    zone_type               zone_type NOT NULL,
    zone_boundary           GEOMETRY(POLYGON, 4326) NOT NULL,
    center_point            GEOMETRY(POINT, 4326),
    radius_meters           DOUBLE PRECISION, -- For circular zones
    alert_on_entry          BOOLEAN DEFAULT TRUE,
    alert_on_exit           BOOLEAN DEFAULT FALSE,
    requires_escort         BOOLEAN DEFAULT FALSE,
    max_visitors            INTEGER, -- Maximum visitors allowed
    current_visitors        INTEGER DEFAULT 0,
    is_active               BOOLEAN DEFAULT TRUE,
    created_by              BIGINT REFERENCES users(id),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Ensure circular zones have radius
    CONSTRAINT chk_circle_radius CHECK (zone_type != 'WILDLIFE_AREA' OR radius_meters IS NOT NULL)
    );

-- Add comments
COMMENT ON TABLE geofence_zones IS 'Defines geofenced areas within the park for safety monitoring';

-- Create trigger for updated_at
CREATE TRIGGER update_geofence_zones_updated_at
    BEFORE UPDATE ON geofence_zones
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create spatial indexes
CREATE INDEX IF NOT EXISTS idx_geofence_zones_boundary
    ON geofence_zones USING GIST (zone_boundary);

CREATE INDEX IF NOT EXISTS idx_geofence_zones_type
    ON geofence_zones(zone_type, is_active);