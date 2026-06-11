-- =====================================================
-- V12: Add payment_tracking_id to bookings table
-- Description: Stores M-Pesa CheckoutRequestID for callback matching
-- =====================================================

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS payment_tracking_id VARCHAR(100);

COMMENT ON COLUMN bookings.payment_tracking_id IS 'M-Pesa CheckoutRequestID for linking STK push callbacks';

-- Index for faster lookup by tracking ID
CREATE INDEX IF NOT EXISTS idx_bookings_payment_tracking_id ON bookings(payment_tracking_id);