-- =====================================================
-- V4: Create emergency_alerts table (VARCHAR for all enums)
-- =====================================================

CREATE TABLE IF NOT EXISTS emergency_alerts (
                                                id                    BIGSERIAL        PRIMARY KEY,
                                                session_id            BIGINT           NOT NULL REFERENCES visitor_sessions(id) ON DELETE CASCADE,
    user_id               BIGINT           NOT NULL REFERENCES users(id),
    alert_type            VARCHAR(50)      NOT NULL,
    alert_status          VARCHAR(50)      NOT NULL DEFAULT 'PENDING',
    priority              VARCHAR(50)               DEFAULT 'HIGH',
    latitude              DOUBLE PRECISION NOT NULL,
    longitude             DOUBLE PRECISION NOT NULL,
    location_point        GEOMETRY(POINT, 4326) GENERATED ALWAYS AS (
                                                                        ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
    ) STORED,
    message               TEXT,
    assigned_ranger_id    BIGINT           REFERENCES users(id),
    responded_at          TIMESTAMP,
    resolved_at           TIMESTAMP,
    resolution_notes      TEXT,
    response_time_seconds INTEGER,
    created_at            TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP                 DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_alert_type CHECK (alert_type IN ('MEDICAL','LOST','WILDLIFE_ENCOUNTER','VEHICLE_BREAKDOWN','ACCIDENT','GENERAL_DISTRESS')),
    CONSTRAINT chk_alert_status CHECK (alert_status IN ('PENDING','RESPONDING','RESOLVED','FALSE_ALARM')),
    CONSTRAINT chk_alert_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    CONSTRAINT chk_responded_after_created CHECK (responded_at IS NULL OR responded_at >= created_at),
    CONSTRAINT chk_resolved_after_responded CHECK (resolved_at IS NULL OR (responded_at IS NOT NULL AND resolved_at >= responded_at))
    );

COMMENT ON TABLE  emergency_alerts IS 'Records all SOS emergency alerts triggered by visitors';
COMMENT ON COLUMN emergency_alerts.alert_type   IS 'Type of emergency: MEDICAL, LOST, WILDLIFE_ENCOUNTER, etc.';
COMMENT ON COLUMN emergency_alerts.priority     IS 'Alert priority based on type and context';
COMMENT ON COLUMN emergency_alerts.response_time_seconds IS 'Response time in seconds; populated by trigger when responded_at is set';

-- updated_at trigger
DROP TRIGGER IF EXISTS update_emergency_alerts_updated_at ON emergency_alerts;
CREATE TRIGGER update_emergency_alerts_updated_at
    BEFORE UPDATE ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- response_time trigger
DROP TRIGGER IF EXISTS trigger_set_response_time ON emergency_alerts;
CREATE OR REPLACE FUNCTION set_response_time_seconds()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.responded_at IS NOT NULL AND OLD.responded_at IS NULL THEN
        NEW.response_time_seconds := EXTRACT(EPOCH FROM (NEW.responded_at - NEW.created_at))::INTEGER;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trigger_set_response_time
    BEFORE UPDATE ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION set_response_time_seconds();

-- SOS notification trigger
DROP TRIGGER IF EXISTS trigger_sos_notification ON emergency_alerts;
CREATE OR REPLACE FUNCTION notify_sos_triggered()
    RETURNS TRIGGER AS $$
BEGIN
UPDATE visitor_sessions
SET sos_triggered = TRUE,
    has_emergency = TRUE
WHERE id = NEW.session_id;
PERFORM pg_notify('sos_alert', json_build_object(
        'alert_id',   NEW.id,
        'session_id', NEW.session_id,
        'alert_type', NEW.alert_type,
        'latitude',   NEW.latitude,
        'longitude',  NEW.longitude,
        'timestamp',  NEW.created_at
    )::text);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trigger_sos_notification
    AFTER INSERT ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION notify_sos_triggered();

-- Indexes
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_status
    ON emergency_alerts(alert_status, created_at DESC)
    WHERE alert_status IN ('PENDING', 'RESPONDING');
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_session ON emergency_alerts(session_id);
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_assigned ON emergency_alerts(assigned_ranger_id, alert_status);
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_created ON emergency_alerts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_point ON emergency_alerts USING GIST (location_point);