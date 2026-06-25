-- =====================================================
-- V14: Insert sample geofence zones for Maasai Mara
-- Description: Populates geofence zones for the park
-- =====================================================

-- Insert sample geofence zones for Maasai Mara
INSERT INTO geofence_zones (zone_name, zone_description, zone_type, zone_boundary, alert_on_entry, alert_on_exit, is_active)
VALUES
    -- SAFE ZONE: Keekorok Lodge Area
    ('Keekorok Lodge Area',
     'Main visitor center and accommodation area - safe zone',
     'SAFE',
     ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(35.2800 -1.5500, 35.2900 -1.5500, 35.2900 -1.5600, 35.2800 -1.5600, 35.2800 -1.5500)')), 4326),
     FALSE, FALSE, TRUE),

    -- DANGER ZONE: Mara River Crossing
    ('Mara River Crossing',
     'High risk area - hippos and crocodiles present',
     'DANGER',
     ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(35.0520 -1.4900, 35.0580 -1.4900, 35.0580 -1.4960, 35.0520 -1.4960, 35.0520 -1.4900)')), 4326),
     TRUE, FALSE, TRUE),

    -- WILDLIFE AREA: Mara Triangle
    ('Mara Triangle',
     'High wildlife density - lions, elephants, cheetahs',
     'WILDLIFE_AREA',
     ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(34.9900 -1.4400, 35.0100 -1.4400, 35.0100 -1.4600, 34.9900 -1.4600, 34.9900 -1.4400)')), 4326),
     TRUE, FALSE, TRUE),

    -- RESTRICTED ZONE: Oloololo Escarpment
    ('Oloololo Escarpment',
     'Steep cliffs - restricted access',
     'RESTRICTED',
     ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(34.8800 -1.4800, 34.8900 -1.4800, 34.8900 -1.4900, 34.8800 -1.4900, 34.8800 -1.4800)')), 4326),
     TRUE, FALSE, TRUE),

    -- EMERGENCY EXIT: Sekenani Gate
    ('Sekenani Gate',
     'Main exit point - emergency evacuation',
     'EMERGENCY_EXIT',
     ST_SetSRID(ST_MakePolygon(ST_GeomFromText('LINESTRING(35.1140 -1.5640, 35.1180 -1.5640, 35.1180 -1.5680, 35.1140 -1.5680, 35.1140 -1.5640)')), 4326),
     FALSE, TRUE, TRUE);

-- Verify the insert
DO $$
DECLARE
zone_count INTEGER;
BEGIN
SELECT COUNT(*) INTO zone_count FROM geofence_zones;
RAISE NOTICE '✅ V14: Inserted % geofence zones for Maasai Mara', zone_count;
END $$;