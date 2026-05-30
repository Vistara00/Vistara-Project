-- =====================================================
-- V6: Create additional indexes for query performance
-- =====================================================

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_users_role_active
    ON users(role, is_active)
    WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_visitor_sessions_checkin
    ON visitor_sessions(check_in_time DESC)
    WHERE is_active = TRUE;

-- Partial index for active alerts (most frequently queried)
CREATE INDEX IF NOT EXISTS idx_active_alerts
    ON emergency_alerts(created_at DESC, priority)
    WHERE alert_status IN ('PENDING', 'RESPONDING');

-- Index for date range queries on location history
CREATE INDEX IF NOT EXISTS idx_location_tracking_date
    ON location_tracking(date_trunc('day', timestamp), session_id);

-- Full-text search index for user names
CREATE INDEX IF NOT EXISTS idx_users_name_search
    ON users USING GIN (to_tsvector('english', full_name || ' ' || COALESCE(email, '')));

-- Index for finding nearby visitors (using PostGIS)
CREATE INDEX IF NOT EXISTS idx_nearby_visitors
    ON visitor_sessions USING GIST (last_known_location)
    WHERE is_active = TRUE;

-- Index for emergency alerts by date and type
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_type_date
    ON emergency_alerts(alert_type, date_trunc('day', created_at));

-- Bloom index for equality queries on multiple columns
CREATE EXTENSION IF NOT EXISTS bloom;
CREATE INDEX IF NOT EXISTS idx_users_bloom
    ON users USING bloom (role, is_active, is_verified);

-- Analyze tables to update statistics
ANALYZE users;
ANALYZE visitor_sessions;
ANALYZE location_tracking;
ANALYZE emergency_alerts;
ANALYZE geofence_zones;