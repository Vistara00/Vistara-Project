-- =====================================================
-- V10: Create bookings table for reservations & payments
-- =====================================================

CREATE TABLE IF NOT EXISTS bookings (
                                        id                      BIGSERIAL PRIMARY KEY,
                                        user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_reference       VARCHAR(50) UNIQUE NOT NULL,
    check_in_date           DATE NOT NULL,
    check_out_date          DATE NOT NULL,
    group_size              INTEGER NOT NULL CHECK (group_size > 0),
    vehicle_registration    VARCHAR(50),
    payment_method          VARCHAR(20) NOT NULL CHECK (payment_method IN ('MPESA', 'E_CITIZEN')),
    amount                  DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    payment_reference       VARCHAR(100),
    payment_status          VARCHAR(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    booking_status          VARCHAR(20) DEFAULT 'PENDING' CHECK (booking_status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    admin_notes             TEXT,
    created_by_admin_id     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

COMMENT ON TABLE bookings IS 'Pre‑visit bookings made by tourists or manually created by admin';
COMMENT ON COLUMN bookings.booking_reference IS 'Unique reference code for the booking (e.g., VST-20250610-001)';
COMMENT ON COLUMN bookings.payment_status IS 'PENDING, PAID, FAILED, REFUNDED';
COMMENT ON COLUMN bookings.booking_status IS 'PENDING, CONFIRMED, CANCELLED, COMPLETED';

-- Indexes
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_bookings_payment_status ON bookings(payment_status);
CREATE INDEX idx_bookings_booking_status ON bookings(booking_status);
CREATE INDEX idx_bookings_reference ON bookings(booking_reference);

-- Trigger for updated_at
CREATE TRIGGER update_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();