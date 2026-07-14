-- =====================================================
-- V7: Create notifications table
-- =====================================================

CREATE TABLE IF NOT EXISTS notifications (
                                             id              BIGSERIAL PRIMARY KEY,
                                             user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    type            VARCHAR(50),
    is_read         BOOLEAN DEFAULT FALSE,
    is_broadcast    BOOLEAN DEFAULT FALSE,
    reference_id    BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);

COMMENT ON COLUMN notifications.type IS 'BOOKING, PAYMENT, CHECKIN, CHECKOUT, BROADCAST, SYSTEM, ALERT, GEOFENCE';