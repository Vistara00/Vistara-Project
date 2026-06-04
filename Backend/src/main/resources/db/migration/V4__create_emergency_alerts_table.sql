-- =====================================================
-- V4: Create emergency_alerts table
-- Description: Stores SOS emergency alerts from visitors
-- =====================================================

-- ENUM types
DO $$ BEGIN
    CREATE TYPE alert_type AS ENUM (
        'MEDICAL', 'LOST', 'WILDLIFE_ENCOUNTER',
        'VEHICLE_BREAKDOWN', 'ACCIDENT', 'GENERAL_DISTRESS'
    );
EXCEPTION WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE alert_status AS ENUM ('PENDING', 'RESPONDING', 'RESOLVED', 'FALSE_ALARM');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE alert_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS emergency_alerts (
    id                    BIGSERIAL      PRIMARY KEY,
    session_id            BIGINT         NOT NULL REFERENCES visitor_sessions(id) ON DELETE CASCADE,
    user_id               BIGINT         NOT NULL REFERENCES users(id),
    alert_type            alert_type     NOT NULL,
    alert_status          alert_status   NOT NULL DEFAULT 'PENDING',
    priority              alert_priority DEFAULT 'HIGH',
    latitude              DOUBLE PRECISION NOT NULL,
    longitude             DOUBLE PRECISION NOT NULL,
    location_point        GEOMETRY(POINT, 4326) GENERATED ALWAYS AS (
                              ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
                          ) STORED,
    message               TEXT,
    assigned_ranger_id    BIGINT         REFERENCES users(id),
    responded_at          TIMESTAMP,
    resolved_at           TIMESTAMP,
    resolution_notes      TEXT,
    -- FIX (Bug 4): Replaced the generated column with a plain nullable integer.
    -- Generated columns cannot safely reference columns with volatile defaults
    -- (created_at uses CURRENT_TIMESTAMP) across all PostgreSQL versions.
    -- This column is now populated by a trigger when responded_at is set.
    response_time_seconds INTEGER,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_responded_after_created  CHECK (
        responded_at IS NULL OR responded_at >= created_at
    ),
    -- FIX (Bug 5): Prevent resolved_at being set when responded_at is still NULL.
    -- Original constraint allowed an alert to be resolved without ever being
    -- responded to. Now both conditions must be satisfied.
    CONSTRAINT chk_resolved_after_responded CHECK (
        resolved_at IS NULL OR (responded_at IS NOT NULL AND resolved_at >= responded_at)
    )
);

-- Add comments
COMMENT ON TABLE  emergency_alerts IS 'Records all SOS emergency alerts triggered by visitors';
COMMENT ON COLUMN emergency_alerts.alert_type             IS 'Type of emergency: MEDICAL, LOST, WILDLIFE_ENCOUNTER, etc.';
COMMENT ON COLUMN emergency_alerts.priority               IS 'Alert priority based on type and context';
COMMENT ON COLUMN emergency_alerts.response_time_seconds  IS 'Response time in seconds; populated by trigger when responded_at is set';

-- Trigger for updated_at
CREATE TRIGGER update_emergency_alerts_updated_at
    BEFORE UPDATE ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- FIX (Bug 4 continued): Trigger to compute response_time_seconds when
-- responded_at is first set.
CREATE OR REPLACE FUNCTION set_response_time_seconds()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.responded_at IS NOT NULL AND OLD.responded_at IS NULL THEN
        NEW.response_time_seconds :=
            EXTRACT(EPOCH FROM (NEW.responded_at - NEW.created_at))::INTEGER;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_response_time
    BEFORE UPDATE ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION set_response_time_seconds();

-- Indexes
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_status
    ON emergency_alerts(alert_status, created_at DESC)
    WHERE alert_status IN ('PENDING', 'RESPONDING');

CREATE INDEX IF NOT EXISTS idx_emergency_alerts_session
    ON emergency_alerts(session_id);

CREATE INDEX IF NOT EXISTS idx_emergency_alerts_assigned
    ON emergency_alerts(assigned_ranger_id, alert_status);

CREATE INDEX IF NOT EXISTS idx_emergency_alerts_created
    ON emergency_alerts(created_at DESC);

-- Spatial index
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_point
    ON emergency_alerts USING GIST (location_point);
