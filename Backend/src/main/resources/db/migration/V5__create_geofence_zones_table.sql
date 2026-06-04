-- =====================================================
-- V5: Create geofence_zones table
-- Description: Defines safe and restricted zones within the park
-- =====================================================

DO $$ BEGIN
    CREATE TYPE zone_type AS ENUM (
        'SAFE', 'RESTRICTED', 'DANGER', 'WILDLIFE_AREA', 'EMERGENCY_EXIT'
    );
EXCEPTION WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS geofence_zones (
    id               BIGSERIAL        PRIMARY KEY,
    zone_name        VARCHAR(255)     NOT NULL,
    zone_description TEXT,
    zone_type        zone_type        NOT NULL,
    zone_boundary    GEOMETRY(POLYGON, 4326) NOT NULL,
    center_point     GEOMETRY(POINT, 4326),
    radius_meters    DOUBLE PRECISION,
    alert_on_entry   BOOLEAN  DEFAULT TRUE,
    alert_on_exit    BOOLEAN  DEFAULT FALSE,
    requires_escort  BOOLEAN  DEFAULT FALSE,
    max_visitors     INTEGER,
    current_visitors INTEGER  DEFAULT 0,
    is_active        BOOLEAN  DEFAULT TRUE,
    created_by       BIGINT   REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- FIX (Bug 6): Replaced the zone_type-specific constraint with a general
    -- structural rule: center_point and radius_meters must either both be
    -- present or both be absent. The original constraint only covered
    -- WILDLIFE_AREA zones, leaving all other circular zones unconstrained.
    -- This also removes the need for the ALTER TABLE patch in V7.
    CONSTRAINT chk_circle_radius CHECK (
        (center_point IS NULL     AND radius_meters IS NULL) OR
        (center_point IS NOT NULL AND radius_meters IS NOT NULL)
    )
);

COMMENT ON TABLE geofence_zones IS 'Defines geofenced areas within the park for safety monitoring';

-- Trigger for updated_at
CREATE TRIGGER update_geofence_zones_updated_at
    BEFORE UPDATE ON geofence_zones
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Spatial index on zone boundary
CREATE INDEX IF NOT EXISTS idx_geofence_zones_boundary
    ON geofence_zones USING GIST (zone_boundary);

CREATE INDEX IF NOT EXISTS idx_geofence_zones_type
    ON geofence_zones(zone_type, is_active);
