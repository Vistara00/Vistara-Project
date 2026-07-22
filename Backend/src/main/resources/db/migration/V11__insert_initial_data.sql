-- =====================================================
-- V11: Insert initial data
-- =====================================================

-- Admin user (password: admin123)
INSERT INTO users (email, password, full_name, phone_number, role, is_verified, created_at)
SELECT
    'admin@vistara.com',
    '$2a$12$nOktnh2V9zu2FVEXFh.P1.fqWWvBdlrQvZ.Dvo1EguYMuDC79TX3G',
    'System Administrator',
    '+254700000000',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@vistara.com');

-- Park ranger (password: ranger123)
INSERT INTO users (email, password, full_name, phone_number, role, is_verified, created_at)
SELECT
    'ranger@vistara.com',
    '$2a$12$ojab0Cg9CBusDsso6Ez0je4TfhuIKksytT15AMZj4f5tm4CZLr25q',
    'Head Park Ranger',
    '+254711111111',
    'PARK_RANGER',
    TRUE,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'ranger@vistara.com');

-- Sample geofence zones
INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, is_active)
SELECT
    'Main Visitor Center',
    'Main visitor center and parking area - safe zone',
    'SAFE',
    ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(36.8219 -1.2921, 36.8225 -1.2921, 36.8225 -1.2927, 36.8219 -1.2927, 36.8219 -1.2921)')), 4326),
    FALSE, FALSE, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Main Visitor Center');

INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, is_active)
SELECT
    'Wildlife Corridor North',
    'High wildlife activity area - visitors must stay on vehicle',
    'WILDLIFE_AREA',
    ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(36.8200 -1.2900, 36.8250 -1.2900, 36.8250 -1.2950, 36.8200 -1.2950, 36.8200 -1.2900)')), 4326),
    TRUE, FALSE, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Wildlife Corridor North');