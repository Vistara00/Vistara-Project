-- =====================================================
-- V6: Create additional indexes for query performance
-- =====================================================

-- Active rangers/tourists lookup
CREATE INDEX IF NOT EXISTS idx_users_role_active
    ON users(role, is_active)
    WHERE is_active = TRUE;

-- FIX (Bug 8): GIN index now references the stable generated column
-- name_search_vector (defined in V1) instead of an inline to_tsvector()
-- expression. The expression approach can cause index-scan mismatches if
-- the search query uses a different text-search config or function form.
CREATE INDEX IF NOT EXISTS idx_users_name_search
    ON users USING GIN (name_search_vector);

-- Recent check-ins
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_checkin
    ON visitor_sessions(check_in_time DESC)
    WHERE is_active = TRUE;

-- FIX (Bug 1): Single, partial GIST index for active visitor location queries.
-- Replaces the unconditional index removed from V2; the WHERE clause makes
-- it far more selective since only active sessions need spatial lookups.
CREATE INDEX IF NOT EXISTS idx_nearby_visitors
    ON visitor_sessions USING GIST (last_known_location)
    WHERE is_active = TRUE;

-- Active alert lookup
CREATE INDEX IF NOT EXISTS idx_active_alerts
    ON emergency_alerts(created_at DESC)
    WHERE alert_status IN ('PENDING', 'RESPONDING');

-- Alert type + date range queries
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_type_date
    ON emergency_alerts(alert_type, created_at);

-- FIX (Bug 7): Removed idx_location_tracking_timestamp_date.
-- idx_location_tracking_timestamp (created in V3) already covers
-- (timestamp DESC). A second B-tree index on (timestamp ASC) is redundant;
-- PostgreSQL can scan the DESC index in reverse for ASC queries.

-- Update planner statistics
ANALYZE users;
ANALYZE visitor_sessions;
ANALYZE location_tracking;
ANALYZE emergency_alerts;
ANALYZE geofence_zones;
