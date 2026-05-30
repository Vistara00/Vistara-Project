-- =====================================================
-- V4: Create emergency_alerts table
-- Description: Stores SOS emergency alerts from visitors
-- =====================================================

-- Create ENUM types for alerts
DO $$ BEGIN
CREATE TYPE alert_type AS ENUM ('MEDICAL', 'LOST', 'WILDLIFE_ENCOUNTER', 'VEHICLE_BREAKDOWN', 'ACCIDENT', 'GENERAL_DISTRESS');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
CREATE TYPE alert_status AS ENUM ('PENDING', 'RESPONDING', 'RESOLVED', 'FALSE_ALARM');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
CREATE TYPE alert_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS emergency_alerts (
                                                id                      BIGSERIAL PRIMARY KEY,
                                                session_id              BIGINT NOT NULL REFERENCES visitor_sessions(id) ON DELETE CASCADE,
    user_id                 BIGINT NOT NULL REFERENCES users(id),
    alert_type              alert_type NOT NULL,
    alert_status            alert_status NOT NULL DEFAULT 'PENDING',
    priority                alert_priority DEFAULT 'HIGH',
    latitude                DOUBLE PRECISION NOT NULL,
    longitude               DOUBLE PRECISION NOT NULL,
    location_point          GEOMETRY(POINT, 4326) GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)) STORED,
    message                 TEXT,
    assigned_ranger_id      BIGINT REFERENCES users(id),
    responded_at            TIMESTAMP,
    resolved_at             TIMESTAMP,
    resolution_notes        TEXT,
    response_time_seconds   INTEGER GENERATED ALWAYS AS (
    EXTRACT(EPOCH FROM (responded_at - created_at))::INTEGER
    ) STORED,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_resolved_after_responded CHECK (resolved_at IS NULL OR resolved_at >= responded_at),
    CONSTRAINT chk_responded_after_created CHECK (responded_at IS NULL OR responded_at >= created_at)
    );

-- Add comments
COMMENT ON TABLE emergency_alerts IS 'Records all SOS emergency alerts triggered by visitors';
COMMENT ON COLUMN emergency_alerts.alert_type IS 'Type of emergency: MEDICAL, LOST, WILDLIFE_ENCOUNTER, etc.';
COMMENT ON COLUMN emergency_alerts.priority IS 'Alert priority based on type and context';
COMMENT ON COLUMN emergency_alerts.response_time_seconds IS 'Calculated response time in seconds (generated column)';

-- Create trigger for updated_at
CREATE TRIGGER update_emergency_alerts_updated_at
    BEFORE UPDATE ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_status
    ON emergency_alerts(alert_status, created_at DESC)
    WHERE alert_status IN ('PENDING', 'RESPONDING');

CREATE INDEX IF NOT EXISTS idx_emergency_alerts_session
    ON emergency_alerts(session_id);

CREATE INDEX IF NOT EXISTS idx_emergency_alerts_assigned
    ON emergency_alerts(assigned_ranger_id, alert_status);

CREATE INDEX IF NOT EXISTS idx_emergency_alerts_created
    ON emergency_alerts(created_at DESC);

-- Create spatial index
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_point
    ON emergency_alerts USING GIST (location_point);