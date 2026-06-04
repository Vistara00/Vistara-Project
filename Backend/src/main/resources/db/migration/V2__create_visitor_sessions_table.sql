-- =====================================================
-- V2: Create visitor_sessions table
-- Description: Tracks visitor check-in/check-out sessions
-- =====================================================

CREATE TABLE IF NOT EXISTS visitor_sessions (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    check_in_time         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time        TIMESTAMP,
    is_active             BOOLEAN   DEFAULT TRUE,
    group_size            INTEGER   DEFAULT 1,
    vehicle_registration  VARCHAR(50),
    last_known_location   GEOMETRY(POINT, 4326),
    last_location_update  TIMESTAMP,
    sos_triggered         BOOLEAN   DEFAULT FALSE,
    has_emergency         BOOLEAN   DEFAULT FALSE,
    notes                 TEXT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_visitor_session_dates CHECK (check_out_time IS NULL OR check_out_time >= check_in_time),
    CONSTRAINT chk_group_size_positive   CHECK (group_size > 0)
);

-- Add comments
COMMENT ON TABLE  visitor_sessions IS 'Tracks each visitor entry/exit session in the park';
COMMENT ON COLUMN visitor_sessions.last_known_location IS 'Last known GPS location stored as PostGIS POINT geometry (longitude, latitude)';
COMMENT ON COLUMN visitor_sessions.sos_triggered       IS 'Whether SOS has been triggered during this session';
COMMENT ON COLUMN visitor_sessions.has_emergency       IS 'Whether an emergency is currently active for this session';

-- Trigger for updated_at
CREATE TRIGGER update_visitor_sessions_updated_at
    BEFORE UPDATE ON visitor_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Index for active sessions
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_active
    ON visitor_sessions(is_active) WHERE is_active = TRUE;

-- Index for user sessions
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_user
    ON visitor_sessions(user_id, is_active);

-- FIX (Bug 1): Removed the unconditional GIST index on last_known_location.
-- A superior partial index (WHERE is_active = TRUE) is created in V6,
-- which is more selective and covers the same query patterns.
