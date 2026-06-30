-- =====================================================
-- V14: Update notification types to include ALERT
-- =====================================================

COMMENT ON COLUMN notifications.type IS 'BOOKING, PAYMENT, CHECKIN, CHECKOUT, BROADCAST, SYSTEM, ALERT, GEOFENCE';

-- No schema changes needed, just updating the comment