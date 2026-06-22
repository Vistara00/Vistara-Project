-- =====================================================
-- V12: Convert remaining ENUM columns to VARCHAR
-- =====================================================

-- 1. Drop views that depend on columns being altered
DROP VIEW IF EXISTS v_active_emergencies CASCADE;
DROP VIEW IF EXISTS v_park_occupancy CASCADE;

-- 2. Drop triggers that may depend on the columns
DROP TRIGGER IF EXISTS trigger_sos_notification ON emergency_alerts;
DROP TRIGGER IF EXISTS trigger_set_response_time ON emergency_alerts;
DROP TRIGGER IF EXISTS trigger_update_session_location ON location_tracking;

-- 3. Drop functions that may depend on the columns (if they reference enum types)
DROP FUNCTION IF EXISTS notify_sos_triggered() CASCADE;
DROP FUNCTION IF EXISTS set_response_time_seconds() CASCADE;
DROP FUNCTION IF EXISTS update_session_last_location() CASCADE;
DROP FUNCTION IF EXISTS get_active_visitors_in_zone(BIGINT) CASCADE;
DROP FUNCTION IF EXISTS get_session_distance_traveled(BIGINT) CASCADE;

-- 4. Convert emergency_alerts columns to VARCHAR
ALTER TABLE emergency_alerts ALTER COLUMN alert_type TYPE VARCHAR(50) USING alert_type::text;
ALTER TABLE emergency_alerts ALTER COLUMN alert_status TYPE VARCHAR(50) USING alert_status::text;
ALTER TABLE emergency_alerts ALTER COLUMN priority TYPE VARCHAR(50) USING priority::text;

-- 5. Convert geofence_zones zone_type to VARCHAR
ALTER TABLE geofence_zones ALTER COLUMN zone_type TYPE VARCHAR(30) USING zone_type::text;

-- 6. Drop remaining enum types
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS alert_type CASCADE;
DROP TYPE IF EXISTS alert_status CASCADE;
DROP TYPE IF EXISTS alert_priority CASCADE;
DROP TYPE IF EXISTS zone_type CASCADE;
DROP TYPE IF EXISTS payment_method CASCADE;

-- 7. Recreate functions that were dropped
CREATE OR REPLACE FUNCTION set_response_time_seconds()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.responded_at IS NOT NULL AND OLD.responded_at IS NULL THEN
        NEW.response_time_seconds := EXTRACT(EPOCH FROM (NEW.responded_at - NEW.created_at))::INTEGER;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION notify_sos_triggered()
    RETURNS TRIGGER AS $$
BEGIN
UPDATE visitor_sessions
SET sos_triggered = TRUE,
    has_emergency = TRUE
WHERE id = NEW.session_id;

PERFORM pg_notify('sos_alert', json_build_object(
        'alert_id',   NEW.id,
        'session_id', NEW.session_id,
        'alert_type', NEW.alert_type,
        'latitude',   NEW.latitude,
        'longitude',  NEW.longitude,
        'timestamp',  NEW.created_at
    )::text);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_session_last_location()
RETURNS TRIGGER AS $$
BEGIN
UPDATE visitor_sessions
SET last_known_location  = NEW.location_point,
    last_location_update = NEW.timestamp
WHERE id = NEW.session_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_active_visitors_in_zone(zone_id BIGINT)
RETURNS INTEGER AS $$
DECLARE
zone_geometry  GEOMETRY;
    visitor_count  INTEGER;
BEGIN
SELECT zone_boundary INTO zone_geometry
FROM geofence_zones
WHERE id = zone_id;

SELECT COUNT(DISTINCT vs.id) INTO visitor_count
FROM visitor_sessions vs
WHERE vs.is_active = TRUE
  AND ST_Within(vs.last_known_location, zone_geometry);

RETURN visitor_count;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_session_distance_traveled(p_session_id BIGINT)
RETURNS DOUBLE PRECISION AS $$
DECLARE
total_distance DOUBLE PRECISION := 0;
    prev_point     GEOMETRY;
    curr_point     GEOMETRY;
    loc_record     RECORD;
BEGIN
FOR loc_record IN
SELECT location_point
FROM   location_tracking
WHERE  session_id = p_session_id
ORDER  BY timestamp ASC
    LOOP
    curr_point := loc_record.location_point;
IF prev_point IS NOT NULL THEN
            total_distance := total_distance +
                ST_Distance(prev_point::geography, curr_point::geography);
END IF;
        prev_point := curr_point;
END LOOP;

RETURN total_distance;
END;
$$ LANGUAGE plpgsql;

-- 8. Recreate triggers
CREATE TRIGGER trigger_set_response_time
    BEFORE UPDATE ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION set_response_time_seconds();

CREATE TRIGGER trigger_sos_notification
    AFTER INSERT ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION notify_sos_triggered();

CREATE TRIGGER trigger_update_session_location
    AFTER INSERT ON location_tracking
    FOR EACH ROW
    EXECUTE FUNCTION update_session_last_location();

-- 9. Recreate views
CREATE OR REPLACE VIEW v_active_emergencies AS
SELECT
    ea.id,
    ea.alert_type,
    ea.priority,
    ea.latitude,
    ea.longitude,
    ea.message,
    u.full_name    AS visitor_name,
    u.phone_number AS visitor_phone,
    vs.group_size,
    vs.check_in_time,
    (EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - ea.created_at)) / 60)::INTEGER AS minutes_since_alert,
        r.full_name    AS assigned_ranger_name
FROM emergency_alerts ea
         JOIN visitor_sessions  vs ON ea.session_id       = vs.id
         JOIN users              u ON vs.user_id           = u.id
         LEFT JOIN users         r ON ea.assigned_ranger_id = r.id
WHERE ea.alert_status IN ('PENDING', 'RESPONDING');

CREATE OR REPLACE VIEW v_park_occupancy AS
SELECT
    COUNT(*)                                          AS total_active_visitors,
    COUNT(*)                                          AS total_groups,
    COUNT(CASE WHEN sos_triggered = TRUE THEN 1 END)  AS active_emergencies,
    MIN(check_in_time)                                AS earliest_checkin,
    MAX(check_in_time)                                AS latest_checkin
FROM visitor_sessions
WHERE is_active = TRUE;

-- 10. Final check
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name IN ('emergency_alerts', 'geofence_zones', 'users')
ORDER BY table_name, ordinal_position;