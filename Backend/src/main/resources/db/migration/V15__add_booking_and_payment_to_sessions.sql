-- =====================================================
-- V16: Add booking and payment columns to visitor_sessions
-- =====================================================

-- Add booking_id column
ALTER TABLE visitor_sessions ADD COLUMN IF NOT EXISTS booking_id BIGINT REFERENCES bookings(id) ON DELETE SET NULL;

-- Add payment columns
ALTER TABLE visitor_sessions ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) CHECK (payment_method IN ('MPESA', 'E_CITIZEN', 'CASH'));
ALTER TABLE visitor_sessions ADD COLUMN IF NOT EXISTS amount DECIMAL(10,2);
ALTER TABLE visitor_sessions ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(100);
ALTER TABLE visitor_sessions ADD COLUMN IF NOT EXISTS is_paid BOOLEAN DEFAULT FALSE;

-- Add constraint for amount
ALTER TABLE visitor_sessions ADD CONSTRAINT chk_amount_positive CHECK (amount IS NULL OR amount > 0);

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_booking ON visitor_sessions(booking_id) WHERE booking_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_user_active ON visitor_sessions(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_visitor_sessions_paid ON visitor_sessions(is_paid, is_active) WHERE is_paid = TRUE;

-- (Optional) Enforce only one active session per user
-- CREATE UNIQUE INDEX uk_visitor_sessions_one_active_per_user
--     ON visitor_sessions(user_id) WHERE is_active = TRUE;