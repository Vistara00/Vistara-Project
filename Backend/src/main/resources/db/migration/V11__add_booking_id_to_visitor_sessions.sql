-- =====================================================
-- V11: Add booking_id to visitor_sessions
-- =====================================================

ALTER TABLE visitor_sessions ADD COLUMN IF NOT EXISTS booking_id BIGINT REFERENCES bookings(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_visitor_sessions_booking
    ON visitor_sessions(booking_id) WHERE booking_id IS NOT NULL;