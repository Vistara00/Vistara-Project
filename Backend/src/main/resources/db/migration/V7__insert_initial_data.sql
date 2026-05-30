-- =====================================================
-- V7: Insert initial data
-- Description: Populates default admin users and geofence zones
-- =====================================================

-- Insert default admin user (password: Admin@123 - change in production!)
-- Password is BCrypt encoded: Admin@123
INSERT INTO users (email, password, full_name, phone_number, role, is_verified, created_at)
SELECT 'admin@vistara.com',
       '$2a$10$r0Yi0vtaM36TLZmwZ5RlUe5XJkXlL9nQ5Y5n5n5n5n5n5n5n5n5O', -- BCrypt hash of Admin@123
       'System Administrator',
       '+254700000000',
       'ADMIN',
       TRUE,
       CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@vistara.com');

-- Insert default park ranger
INSERT INTO users (email, password, full_name, phone_number, role, is_verified, created_at)
SELECT 'ranger@vistara.com',
       '$2a$10$r0Yi0vtaM36TLZmwZ5RlUe5XJkXlL9nQ5Y5n5n5n5n5n5n5n5O',
       'Head Park Ranger',
       '+254711111111',
       'PARK_RANGER',
       TRUE,
       CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'ranger@vistara.com');

-- Insert default geofence zones (example boundaries)
-- Note: These are example coordinates - replace with actual park boundaries

-- Safe Zone 1: Main visitor center area
INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, is_active)
SELECT 'Main Visitor Center',
       'Main visitor center and parking area - safe zone',
       'SAFE',
       ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(36.8219 -1.2921, 36.8225 -1.2921, 36.8225 -1.2927, 36.8219 -1.2927, 36.8219 -1.2921)')), 4326),
       FALSE,
       FALSE,
       TRUE
    WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Main Visitor Center');

-- Restricted Zone: Wildlife corridor
INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, is_active)
SELECT 'Wildlife Corridor North',
       'High wildlife activity area - visitors must stay on vehicle',
       'WILDLIFE_AREA',
       ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(36.8200 -1.2900, 36.8250 -1.2900, 36.8250 -1.2950, 36.8200 -1.2950, 36.8200 -1.2900)')), 4326),
       TRUE,
       FALSE,
       TRUE
    WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Wildlife Corridor North');

-- Danger Zone: Steep cliffs area
INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, requires_escort, is_active)
SELECT 'Cliff Edge Danger Zone',
       'Steep cliffs - immediate danger - entry forbidden',
       'DANGER',
       ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(36.8180 -1.2930, 36.8190 -1.2930, 36.8190 -1.2940, 36.8180 -1.2940, 36.8180 -1.2930)')), 4326),
       TRUE,
       FALSE,
       TRUE,
       TRUE
    WHERE NOT EXISTS (SELECT 1 FROM geofence_zones WHERE zone_name = 'Cliff Edge Danger Zone');