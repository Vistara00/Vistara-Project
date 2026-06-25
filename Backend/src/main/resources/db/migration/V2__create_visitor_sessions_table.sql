-- =====================================================
-- V2: Create visitor_sessions table
-- =====================================================

CREATE TABLE IF NOT EXISTS visitor_sessions (
                                                id                      BIGSERIAL PRIMARY KEY,
                                                user_id                 BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id              BIGINT    REFERENCES bookings(id) ON DELETE SET NULL,

    -- Check-in/out timestamps
    check_in_time           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time          TIMESTAMP,
    is_active               BOOLEAN   DEFAULT TRUE,

    -- Visitor details
    group_size              INTEGER   DEFAULT 1,
    vehicle_registration    VARCHAR(50),

    -- Location tracking
    last_known_location     GEOMETRY(POINT, 4326),
    last_location_update    TIMESTAMP,

    -- Emergency flags
    sos_triggered           BOOLEAN   DEFAULT FALSE,
    has_emergency           BOOLEAN   DEFAULT FALSE,

    -- Payment information (simplified for quick access)
    payment_method          VARCHAR(20) CHECK (payment_method IN ('MPESA', 'E_CITIZEN', 'CASH')),
    amount                  DECIMAL(10,2),
    payment_reference       VARCHAR(100),
    is_paid                 BOOLEAN   DEFAULT FALSE,

    -- Additional notes
    notes                   TEXT,

    -- Audit timestamps
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_visitor_session_dates CHECK (check_out_time IS NULL OR check_out_time >= check_in_time),
    CONSTRAINT chk_group_size_positive   CHECK (group_size > 0),
    CONSTRAINT chk_amount_positive       CHECK (amount IS NULL OR amount > 0)
    );

-- Add comments
COMMENT ON TABLE  visitor_sessions IS 'Tracks each active visitor entry/exit session in the park';
COMMENT ON COLUMN visitor_sessions.booking_id IS 'Reference to the booking (NULL for walk-ins)';
COMMENT ON COLUMN visitor_sessions.last_known_location IS 'Last known GPS location stored as PostGIS POINT geometry';
COMMENT ON COLUMN visitor_sessions.sos_triggered IS 'Whether SOS has been triggered during this session';
COMMENT ON COLUMN visitor_sessions.has_emergency IS 'Whether an emergency is currently active for this session';
COMMENT ON COLUMN visitor_sessions.payment_method IS 'Payment method used for this session (MPESA, E_CITIZEN, CASH)';
COMMENT ON COLUMN visitor_sessions.amount IS 'Amount paid for this session';
COMMENT ON COLUMN visitor_sessions.is_paid IS 'Whether payment has been confirmed';

-- Trigger for updated_at
CREATE TRIGGER update_visitor_sessions_updated_at
    BEFORE UPDATE ON visitor_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Indexes for performance
-- =====================================================

-- Index for active sessions (most common query)
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_active
    ON visitor_sessions(is_active) WHERE is_active = TRUE;

-- Index for user sessions (to find active sessions per user)
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_user_active
    ON visitor_sessions(user_id, is_active);

-- Index for booking reference (when checking in from booking)
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_booking
    ON visitor_sessions(booking_id) WHERE booking_id IS NOT NULL;

-- Index for check-in time (for dashboard reports)
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_checkin
    ON visitor_sessions(check_in_time DESC);

-- Index for payment status (for reporting)
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_paid
    ON visitor_sessions(is_paid, is_active) WHERE is_paid = TRUE;

-- Index for emergency sessions
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_emergency
    ON visitor_sessions(sos_triggered, is_active) WHERE sos_triggered = TRUE;

-- Spatial index for location queries (active sessions only)
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_location
    ON visitor_sessions USING GIST (last_known_location) WHERE is_active = TRUE;

-- =====================================================
-- (Optional) Enforce only one active session per user
-- Uncomment if you want to enforce this at the database level
-- =====================================================
-- CREATE UNIQUE INDEX uk_visitor_sessions_one_active_per_user
--     ON visitor_sessions(user_id) WHERE is_active = TRUE;