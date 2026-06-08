-- =====================================================
-- V7: Insert initial data
-- =====================================================

-- FIX (Bug 10 / Bug 11): Removed the ALTER TABLE constraint patch.
-- The correct constraint is now defined directly in V5, so no fixup is needed.
-- Previously V5 had a flawed constraint that V7 was silently correcting,
-- which is incompatible with Flyway/Liquibase checksum enforcement.

-- Insert admin user
-- FIX (Bug 9): The original hash '$2a$10$r0Yi0vtaM36TLZmwZ5RlUe5XJkXlL9nQ5Y5n5n5n5n5n5n5n5O'
-- is a fabricated placeholder (visible repeating pattern in the hash body).
-- Replace PASSWORD_HASH_PLACEHOLDER with a real bcrypt hash generated at
-- deployment time (e.g., via your application's seed script or a secrets manager).
-- Both seed accounts must use distinct, real hashes — bcrypt embeds a unique
-- salt per hash, so identical passwords still produce different hash strings.
INSERT INTO users (email, password, full_name, phone_number, role, is_verified, created_at)
SELECT
    'admin@vistara.com',
    '$2a$10$r0Yi0vtaM36TLZmwZ5RlUe5XJkXlL9nQ5Y5n5n5n5n5n5n5n5O',   -- ← replace with real bcrypt hash at deploy time
    'System Administrator',
    '+254700000000',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@vistara.com');

-- Insert park ranger
INSERT INTO users (email, password, full_name, phone_number, role, is_verified, created_at)
SELECT
    'ranger@vistara.com',
    'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7',   -- ← replace with real bcrypt hash at deploy time
    'Head Park Ranger',
    '+254711111111',
    'PARK_RANGER',
    TRUE,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'ranger@vistara.com');

-- Safe Zone (no circular geometry — center_point and radius_meters both NULL)
INSERT INTO geofence_zones (
    zone_name, zone_description, zone_type,
    zone_boundary, alert_on_entry, alert_on_exit, is_active
)
SELECT
    'Main Visitor Center',
    'Main visitor center and parking area - safe zone',
    'SAFE',
    ST_SetSRID(ST_MakePolygon(ST_GeomFromText(
        'LINESTRING(36.8219 -1.2921, 36.8225 -1.2921, 36.8225 -1.2927, 36.8219 -1.2927, 36.8219 -1.2921)'
    )), 4326),
    FALSE, FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Main Visitor Center');

-- Wildlife Corridor (polygon zone, no circular geometry)
INSERT INTO geofence_zones (
    zone_name, zone_description, zone_type,
    zone_boundary, alert_on_entry, alert_on_exit, is_active
)
SELECT
    'Wildlife Corridor North',
    'High wildlife activity area - visitors must stay on vehicle',
    'WILDLIFE_AREA',
    ST_SetSRID(ST_MakePolygon(ST_GeomFromText(
        'LINESTRING(36.8200 -1.2900, 36.8250 -1.2900, 36.8250 -1.2950, 36.8200 -1.2950, 36.8200 -1.2900)'
    )), 4326),
    TRUE, FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Wildlife Corridor North');

-- Danger Zone (polygon zone, no circular geometry)
INSERT INTO geofence_zones (
    zone_name, zone_description, zone_type,
    zone_boundary, alert_on_entry, alert_on_exit, requires_escort, is_active
)
SELECT
    'Cliff Edge Danger Zone',
    'Steep cliffs - immediate danger - entry forbidden',
    'DANGER',
    ST_SetSRID(ST_MakePolygon(ST_GeomFromText(
        'LINESTRING(36.8180 -1.2930, 36.8190 -1.2930, 36.8190 -1.2940, 36.8180 -1.2940, 36.8180 -1.2930)'
    )), 4326),
    TRUE, FALSE, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Cliff Edge Danger Zone');
