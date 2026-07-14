-- V13__add_checkin_status_to_bookings.sql

-- Add checkin_status column to bookings table
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS checkin_status BOOLEAN DEFAULT FALSE;

-- Update existing bookings (if any were checked in but not marked)
UPDATE bookings
SET checkin_status = FALSE
WHERE checkin_status IS NULL;

-- Add comment
COMMENT ON COLUMN bookings.checkin_status IS 'Indicates if the booking has been checked in';