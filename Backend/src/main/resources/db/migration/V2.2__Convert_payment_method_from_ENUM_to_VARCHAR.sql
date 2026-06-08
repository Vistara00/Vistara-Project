-- =====================================================
-- V2.2: Convert payment_method from ENUM to VARCHAR
-- =====================================================

-- 1. Remove the column default (depends on enum)
ALTER TABLE visitor_sessions
    ALTER COLUMN payment_method DROP DEFAULT;

-- 2. Convert the column to VARCHAR(20) using the enum's text representation
ALTER TABLE visitor_sessions
ALTER COLUMN payment_method TYPE VARCHAR(20)
    USING payment_method::text;

-- 3. Add a check constraint to ensure only valid values are stored
ALTER TABLE visitor_sessions
    ADD CONSTRAINT chk_payment_method
        CHECK (payment_method IN ('MPESA', 'E_CITIZEN'));

-- 4. (Optional) Drop the enum type if it is no longer used elsewhere
DROP TYPE IF EXISTS payment_method CASCADE;