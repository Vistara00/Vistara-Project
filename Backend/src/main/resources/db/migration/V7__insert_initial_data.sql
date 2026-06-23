-- =====================================================
-- V7: Insert initial data
-- =====================================================

-- Admin user
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

-- Park ranger
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

-- Safe Zone
INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, is_active)
SELECT
    'Main Visitor Center',
    'Main visitor center and parking area - safe zone',
    'SAFE',
    ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(36.8219 -1.2921, 36.8225 -1.2921, 36.8225 -1.2927, 36.8219 -1.2927, 36.8219 -1.2921)')), 4326),
    FALSE, FALSE, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Main Visitor Center');

-- Wildlife Corridor
INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, is_active)
SELECT
    'Wildlife Corridor North',
    'High wildlife activity area - visitors must stay on vehicle',
    'WILDLIFE_AREA',
    ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(36.8200 -1.2900, 36.8250 -1.2900, 36.8250 -1.2950, 36.8200 -1.2950, 36.8200 -1.2900)')), 4326),
    TRUE, FALSE, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Wildlife Corridor North');

-- Danger Zone
INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, requires_escort, is_active)
SELECT
    'Cliff Edge Danger Zone',
    'Steep cliffs - immediate danger - entry forbidden',
    'DANGER',
    ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(36.8180 -1.2930, 36.8190 -1.2930, 36.8190 -1.2940, 36.8180 -1.2940, 36.8180 -1.2930)')), 4326),
    TRUE, FALSE, TRUE, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Cliff Edge Danger Zone');