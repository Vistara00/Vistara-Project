-- =====================================================
-- V6: Create geofence_zones table
-- =====================================================

CREATE TABLE IF NOT EXISTS geofence_zones (
                                              id               BIGSERIAL PRIMARY KEY,
                                              zone_name        VARCHAR(255) NOT NULL,
    zone_description TEXT,
    zone_type        VARCHAR(30) NOT NULL,
    zone_boundary    GEOMETRY(POLYGON, 4326) NOT NULL,
    center_point     GEOMETRY(POINT, 4326),
    radius_meters    DOUBLE PRECISION,
    alert_on_entry   BOOLEAN DEFAULT TRUE,
    alert_on_exit    BOOLEAN DEFAULT FALSE,
    requires_escort  BOOLEAN DEFAULT FALSE,
    max_visitors     INTEGER,
    current_visitors INTEGER DEFAULT 0,
    is_active        BOOLEAN DEFAULT TRUE,
    created_by       BIGINT REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_zone_type CHECK (zone_type IN ('SAFE','RESTRICTED','DANGER','WILDLIFE_AREA','EMERGENCY_EXIT'))
    );

CREATE TRIGGER update_geofence_zones_updated_at
    BEFORE UPDATE ON geofence_zones
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_geofence_zones_boundary ON geofence_zones USING GIST (zone_boundary);
CREATE INDEX idx_geofence_zones_type ON geofence_zones(zone_type, is_active);