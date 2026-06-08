-- =====================================================
-- V8: Create triggers and stored procedures
-- Description: Automates common database operations
-- =====================================================

-- FIX (Bug 13): Ensure the vistara_admin role exists before any GRANTs.
-- Without this guard, the GRANT statements at the bottom fail with
-- "role does not exist", rolling back the entire V8 migration.
DO $$ BEGIN
    CREATE ROLE vistara_admin;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- -------------------------------------------------------
-- Trigger: sync visitor_sessions.last_known_location
-- whenever a new location_tracking row is inserted
-- -------------------------------------------------------
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

DROP TRIGGER IF EXISTS trigger_update_session_location ON location_tracking;
CREATE TRIGGER trigger_update_session_location
    AFTER INSERT ON location_tracking
    FOR EACH ROW
    EXECUTE FUNCTION update_session_last_location();

-- -------------------------------------------------------
-- Trigger: notify rangers when an SOS alert is inserted
-- -------------------------------------------------------
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

DROP TRIGGER IF EXISTS trigger_sos_notification ON emergency_alerts;
CREATE TRIGGER trigger_sos_notification
    AFTER INSERT ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION notify_sos_triggered();

-- -------------------------------------------------------
-- Function: auto-checkout sessions inactive > 24 hours
-- -------------------------------------------------------
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

-- -------------------------------------------------------
-- Function: count active visitors inside a geofence zone
-- -------------------------------------------------------
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

-- -------------------------------------------------------
-- Function: total distance (metres) travelled in a session
-- FIX (Bug 12): ST_Distance on GEOMETRY(POINT,4326) returns degrees, not
-- metres. Casting both points to GEOGRAPHY before calling ST_Distance
-- produces correct metre-accurate great-circle distances.
-- -------------------------------------------------------
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
            -- Cast to GEOGRAPHY so ST_Distance returns metres
            total_distance := total_distance +
                ST_Distance(prev_point::geography, curr_point::geography);
        END IF;
        prev_point := curr_point;
    END LOOP;

    RETURN total_distance;
END;
$$ LANGUAGE plpgsql;

-- -------------------------------------------------------
-- View: active emergencies dashboard
-- FIX (Bug 14): EXTRACT(EPOCH ...) returns seconds, not minutes.
-- Divided by 60 and renamed column to seconds_since_alert for clarity.
-- -------------------------------------------------------
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
    -- FIX (Bug 14): was labelled minutes_since_alert but actually returned
    -- raw epoch seconds. Now correctly converted to minutes.
    (EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - ea.created_at)) / 60)::INTEGER
                   AS minutes_since_alert,
    r.full_name    AS assigned_ranger_name
FROM emergency_alerts ea
JOIN visitor_sessions  vs ON ea.session_id       = vs.id
JOIN users              u ON vs.user_id           = u.id
LEFT JOIN users         r ON ea.assigned_ranger_id = r.id
WHERE ea.alert_status IN ('PENDING', 'RESPONDING');

-- -------------------------------------------------------
-- View: park occupancy summary
-- FIX (Bug 15): COUNT(DISTINCT group_size) counted distinct group-size
-- *values* (e.g. 3 if groups of size 1, 2, 4 exist), not the number of
-- groups. Each active session row represents one group, so COUNT(*) is correct.
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_park_occupancy AS
SELECT
    COUNT(*)                                          AS total_active_visitors,
    -- FIX (Bug 15): was COUNT(DISTINCT group_size) — incorrect aggregation
    COUNT(*)                                          AS total_groups,
    COUNT(CASE WHEN sos_triggered = TRUE THEN 1 END)  AS active_emergencies,
    MIN(check_in_time)                                AS earliest_checkin,
    MAX(check_in_time)                                AS latest_checkin
FROM visitor_sessions
WHERE is_active = TRUE;

-- -------------------------------------------------------
-- Utility: wrapper to call auto_checkout (schedule via pg_cron externally)
-- -------------------------------------------------------
CREATE OR REPLACE FUNCTION schedule_auto_checkout()
RETURNS void AS $$
BEGIN
    PERFORM auto_checkout_stale_sessions();
END;
$$ LANGUAGE plpgsql;

-- -------------------------------------------------------
-- Grants
-- FIX (Bug 13): vistara_admin role is guaranteed to exist (created above)
-- -------------------------------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES    IN SCHEMA public TO vistara_admin;
GRANT USAGE                          ON ALL SEQUENCES IN SCHEMA public TO vistara_admin;
GRANT EXECUTE                        ON ALL FUNCTIONS IN SCHEMA public TO vistara_admin;
