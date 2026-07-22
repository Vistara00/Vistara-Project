-- =====================================================
-- V10: Create views
-- =====================================================

-- Active emergencies view
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

-- Park occupancy view
CREATE OR REPLACE VIEW v_park_occupancy AS
SELECT
    COUNT(*)                                          AS total_active_visitors,
    COUNT(*)                                          AS total_groups,
    COUNT(CASE WHEN sos_triggered = TRUE THEN 1 END)  AS active_emergencies,
    MIN(check_in_time)                                AS earliest_checkin,
    MAX(check_in_time)                                AS latest_checkin
FROM visitor_sessions
WHERE is_active = TRUE;