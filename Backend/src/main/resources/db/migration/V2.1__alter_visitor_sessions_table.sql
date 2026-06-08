-- =====================================================
-- V2.1: Add booking/payment fields to visitor_sessions
-- =====================================================

DO $$ BEGIN
    CREATE TYPE payment_method AS ENUM ('MPESA', 'E_CITIZEN');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

ALTER TABLE visitor_sessions
    ADD COLUMN IF NOT EXISTS payment_method    payment_method,
    ADD COLUMN IF NOT EXISTS amount            DECIMAL(10,2),
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(100),
    ADD COLUMN IF NOT EXISTS is_paid           BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS booking_notes     TEXT;

CREATE INDEX IF NOT EXISTS idx_visitor_sessions_payment_method
    ON visitor_sessions(payment_method) WHERE payment_method IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_visitor_sessions_is_paid
    ON visitor_sessions(is_paid) WHERE is_paid = FALSE;