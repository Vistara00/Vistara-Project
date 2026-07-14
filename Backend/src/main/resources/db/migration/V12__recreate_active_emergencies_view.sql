-- =====================================================
-- V12: Recreate active emergencies view
-- =====================================================

-- Drop the view if it exists
DROP VIEW IF EXISTS v_active_emergencies CASCADE;

-- Recreate the view with proper column definitions
CREATE OR REPLACE VIEW v_active_emergencies AS
SELECT
    ea.id,
    ea.alert_type,
    ea.alert_status,
    ea.priority,
    ea.latitude,
    ea.longitude,
    ea.message,
    u.full_name    AS visitor_name,
    u.email        AS visitor_email,
    u.phone_number AS visitor_phone,
    vs.group_size,
    vs.check_in_time,
    (EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - ea.created_at)) / 60)::INTEGER AS minutes_since_alert,
        r.full_name    AS assigned_ranger_name,
    ea.created_at
FROM emergency_alerts ea
         JOIN visitor_sessions  vs ON ea.session_id = vs.id
         JOIN users              u ON vs.user_id = u.id
         LEFT JOIN users         r ON ea.assigned_ranger_id = r.id
WHERE ea.alert_status IN ('PENDING', 'RESPONDING');