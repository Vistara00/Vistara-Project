-- =====================================================
-- V6: Create additional indexes for query performance
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_users_role_active
    ON users(role, is_active) WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_users_name_search
    ON users USING GIN (name_search_vector);

CREATE INDEX IF NOT EXISTS idx_visitor_sessions_checkin
    ON visitor_sessions(check_in_time DESC) WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_nearby_visitors
    ON visitor_sessions USING GIST (last_known_location) WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_active_alerts
    ON emergency_alerts(created_at DESC) WHERE alert_status IN ('PENDING', 'RESPONDING');

CREATE INDEX IF NOT EXISTS idx_emergency_alerts_type_date
    ON emergency_alerts(alert_type, created_at);

ANALYZE users;
ANALYZE visitor_sessions;
ANALYZE location_tracking;
ANALYZE emergency_alerts;
ANALYZE geofence_zones;