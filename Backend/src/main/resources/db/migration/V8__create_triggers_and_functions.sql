-- =====================================================
-- V8: Create triggers and stored procedures
-- Description: Automates common database operations
-- =====================================================

-- Function to automatically update last_location_update when new location is added
CREATE OR REPLACE FUNCTION update_session_last_location()
RETURNS TRIGGER AS $$
BEGIN
UPDATE visitor_sessions
SET last_known_location = NEW.location_point,
    last_location_update = NEW.timestamp
WHERE id = NEW.session_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS trigger_update_session_location ON location_tracking;
CREATE TRIGGER trigger_update_session_location
    AFTER INSERT ON location_tracking
    FOR EACH ROW
    EXECUTE FUNCTION update_session_last_location();

-- Function to auto-generate notification when SOS is triggered
CREATE OR REPLACE FUNCTION notify_sos_triggered()
RETURNS TRIGGER AS $$
BEGIN
    -- Update the visitor session to mark SOS triggered
UPDATE visitor_sessions
SET sos_triggered = TRUE,
    has_emergency = TRUE
WHERE id = NEW.session_id;

-- You can also send PostgreSQL notifications for real-time apps
PERFORM pg_notify('sos_alert', json_build_object(
        'alert_id', NEW.id,
        'session_id', NEW.session_id,
        'alert_type', NEW.alert_type,
        'latitude', NEW.latitude,
        'longitude', NEW.longitude,
        'timestamp', NEW.created_at
    )::text);

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_sos_notification ON emergency_alerts;
CREATE TRIGGER trigger_sos_notification
    AFTER INSERT ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION notify_sos_triggered();

-- Function to auto-checkout stale sessions (visitors who haven't checked out)
CREATE OR REPLACE FUNCTION auto_checkout_stale_sessions()
RETURNS void AS $$
BEGIN
UPDATE visitor_sessions
SET is_active = FALSE,
    check_out_time = CURRENT_TIMESTAMP,
    notes = 'Auto checked out due to inactivity > 24 hours'
WHERE is_active = TRUE
  AND last_location_update < CURRENT_TIMESTAMP - INTERVAL '24 hours';
END;
$$ LANGUAGE plpgsql;

-- Function to get count of active visitors in a geofence zone
CREATE OR REPLACE FUNCTION get_active_visitors_in_zone(zone_id BIGINT)
RETURNS INTEGER AS $$
DECLARE
zone_geometry GEOMETRY;
    visitor_count INTEGER;
BEGIN
SELECT zone_boundary INTO zone_geometry FROM geofence_zones WHERE id = zone_id;

SELECT COUNT(DISTINCT vs.id) INTO visitor_count
FROM visitor_sessions vs
WHERE vs.is_active = TRUE
  AND ST_Within(vs.last_known_location, zone_geometry);

RETURN visitor_count;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate distance traveled during a session
CREATE OR REPLACE FUNCTION get_session_distance_traveled(p_session_id BIGINT)
RETURNS DOUBLE PRECISION AS $$
DECLARE
total_distance DOUBLE PRECISION := 0;
    prev_point GEOMETRY;
    curr_point GEOMETRY;
    loc_record RECORD;
BEGIN
FOR loc_record IN
SELECT location_point
FROM location_tracking
WHERE session_id = p_session_id
ORDER BY timestamp ASC
    LOOP
    curr_point := loc_record.location_point;
IF prev_point IS NOT NULL THEN
            total_distance := total_distance + ST_Distance(prev_point, curr_point);
END IF;
        prev_point := curr_point;
END LOOP;

RETURN total_distance;
END;
$$ LANGUAGE plpgsql;

-- Create view for active emergencies dashboard
CREATE OR REPLACE VIEW v_active_emergencies AS
SELECT
    ea.id,
    ea.alert_type,
    ea.priority,
    ea.latitude,
    ea.longitude,
    ea.message,
    u.full_name AS visitor_name,
    u.phone_number AS visitor_phone,
    vs.group_size,
    vs.check_in_time,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - ea.created_at))::INTEGER AS minutes_since_alert,
        r.full_name AS assigned_ranger_name
FROM emergency_alerts ea
         JOIN visitor_sessions vs ON ea.session_id = vs.id
         JOIN users u ON vs.user_id = u.id
         LEFT JOIN users r ON ea.assigned_ranger_id = r.id
WHERE ea.alert_status IN ('PENDING', 'RESPONDING');

-- Create view for park occupancy summary
CREATE OR REPLACE VIEW v_park_occupancy AS
SELECT
    COUNT(*) AS total_active_visitors,
    COUNT(DISTINCT group_size) AS total_groups,
    COUNT(CASE WHEN sos_triggered = TRUE THEN 1 END) AS active_emergencies,
    MIN(check_in_time) AS earliest_checkin,
    MAX(check_in_time) AS latest_checkin
FROM visitor_sessions
WHERE is_active = TRUE;

-- Schedule auto-checkout job (run every hour)
-- Note: In production, use pg_cron or external scheduler
CREATE OR REPLACE FUNCTION schedule_auto_checkout()
RETURNS void AS $$
BEGIN
    PERFORM auto_checkout_stale_sessions();
END;
$$ LANGUAGE plpgsql;

-- Grant permissions (adjust based on your database user)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO vistara_admin;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO vistara_admin;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO vistara_admin;