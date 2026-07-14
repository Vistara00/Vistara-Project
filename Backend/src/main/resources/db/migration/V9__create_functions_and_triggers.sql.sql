-- =====================================================
-- V9: Create functions and triggers
-- =====================================================

-- Response time trigger for emergency_alerts
CREATE OR REPLACE FUNCTION set_response_time_seconds()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.responded_at IS NOT NULL AND OLD.responded_at IS NULL THEN
        NEW.response_time_seconds := EXTRACT(EPOCH FROM (NEW.responded_at - NEW.created_at))::INTEGER;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_response_time
    BEFORE UPDATE ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION set_response_time_seconds();

-- SOS notification trigger
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

CREATE TRIGGER trigger_sos_notification
    AFTER INSERT ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION notify_sos_triggered();

-- Update session location from location_tracking
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

CREATE TRIGGER trigger_update_session_location
    AFTER INSERT ON location_tracking
    FOR EACH ROW
    EXECUTE FUNCTION update_session_last_location();

-- Auto-checkout stale sessions
CREATE OR REPLACE FUNCTION auto_checkout_stale_sessions()
RETURNS void AS $$
BEGIN
UPDATE visitor_sessions
SET is_active      = FALSE,
    check_out_time = CURRENT_TIMESTAMP,
    notes          = 'Auto checked out due to inactivity > 24 hours'
WHERE is_active = TRUE
  AND last_location_update < CURRENT_TIMESTAMP - INTERVAL '24 hours';
END;
$$ LANGUAGE plpgsql;

-- Get active visitors in zone
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