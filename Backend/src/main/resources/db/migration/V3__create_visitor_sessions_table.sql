-- =====================================================
-- V3: Create visitor_sessions table
-- =====================================================

CREATE TABLE IF NOT EXISTS visitor_sessions (
                                                id                      BIGSERIAL PRIMARY KEY,
                                                user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id              BIGINT REFERENCES bookings(id) ON DELETE SET NULL,
    check_in_time           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time          TIMESTAMP,
    is_active               BOOLEAN DEFAULT TRUE,
    group_size              INTEGER DEFAULT 1,
    vehicle_registration    VARCHAR(50),
    last_known_location     GEOMETRY(POINT, 4326),
    last_location_update    TIMESTAMP,
    sos_triggered           BOOLEAN DEFAULT FALSE,
    has_emergency           BOOLEAN DEFAULT FALSE,
    payment_method          VARCHAR(20) CHECK (payment_method IN ('MPESA', 'E_CITIZEN', 'CASH')),
    amount                  DECIMAL(10,2),
    payment_reference       VARCHAR(100),
    is_paid                 BOOLEAN DEFAULT FALSE,
    notes                   TEXT,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_visitor_session_dates CHECK (check_out_time IS NULL OR check_out_time >= check_in_time),
    CONSTRAINT chk_group_size_positive CHECK (group_size > 0),
    CONSTRAINT chk_amount_positive CHECK (amount IS NULL OR amount > 0)
    );

CREATE TRIGGER update_visitor_sessions_updated_at
    BEFORE UPDATE ON visitor_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_visitor_sessions_active ON visitor_sessions(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_visitor_sessions_user_active ON visitor_sessions(user_id, is_active);
CREATE INDEX idx_visitor_sessions_booking ON visitor_sessions(booking_id) WHERE booking_id IS NOT NULL;
CREATE INDEX idx_visitor_sessions_checkin ON visitor_sessions(check_in_time DESC);
CREATE INDEX idx_visitor_sessions_paid ON visitor_sessions(is_paid, is_active) WHERE is_paid = TRUE;
CREATE INDEX idx_visitor_sessions_emergency ON visitor_sessions(sos_triggered, is_active) WHERE sos_triggered = TRUE;
CREATE INDEX idx_visitor_sessions_location ON visitor_sessions USING GIST (last_known_location) WHERE is_active = TRUE;