-- =====================================================
-- V2: Create visitor_sessions table (without payment fields)
-- Description: Tracks active visitor check‑in/check‑out sessions
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

COMMENT ON TABLE  visitor_sessions IS 'Tracks each active visitor entry/exit session in the park';
COMMENT ON COLUMN visitor_sessions.last_known_location IS 'Last known GPS location stored as PostGIS POINT geometry';
COMMENT ON COLUMN visitor_sessions.sos_triggered       IS 'Whether SOS has been triggered during this session';
COMMENT ON COLUMN visitor_sessions.has_emergency       IS 'Whether an emergency is currently active for this session';

-- Trigger for updated_at
CREATE TRIGGER update_visitor_sessions_updated_at
    BEFORE UPDATE ON visitor_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Indexes
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_active
    ON visitor_sessions(is_active) WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_visitor_sessions_user
    ON visitor_sessions(user_id, is_active);