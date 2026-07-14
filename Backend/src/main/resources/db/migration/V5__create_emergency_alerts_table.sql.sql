-- =====================================================
-- V5: Create emergency_alerts table (VARCHAR for all enums)
-- =====================================================

CREATE TABLE IF NOT EXISTS emergency_alerts (
                                                id                    BIGSERIAL PRIMARY KEY,
                                                session_id            BIGINT NOT NULL REFERENCES visitor_sessions(id) ON DELETE CASCADE,
    user_id               BIGINT NOT NULL REFERENCES users(id),
    alert_type            VARCHAR(50) NOT NULL,
    alert_status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    priority              VARCHAR(50) DEFAULT 'HIGH',
    latitude              DOUBLE PRECISION NOT NULL,
    longitude             DOUBLE PRECISION NOT NULL,
    location_point        GEOMETRY(POINT, 4326) GENERATED ALWAYS AS (
                                                                        ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
    ) STORED,
    message               TEXT,
    assigned_ranger_id    BIGINT REFERENCES users(id),
    responded_at          TIMESTAMP,
    resolved_at           TIMESTAMP,
    resolution_notes      TEXT,
    response_time_seconds INTEGER,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_alert_type CHECK (alert_type IN ('MEDICAL','LOST','WILDLIFE_ENCOUNTER','VEHICLE_BREAKDOWN','ACCIDENT','GENERAL_DISTRESS')),
    CONSTRAINT chk_alert_status CHECK (alert_status IN ('PENDING','RESPONDING','RESOLVED','FALSE_ALARM')),
    CONSTRAINT chk_alert_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    CONSTRAINT chk_responded_after_created CHECK (responded_at IS NULL OR responded_at >= created_at),
    CONSTRAINT chk_resolved_after_responded CHECK (resolved_at IS NULL OR (responded_at IS NOT NULL AND resolved_at >= responded_at))
    );

CREATE TRIGGER update_emergency_alerts_updated_at
    BEFORE UPDATE ON emergency_alerts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_emergency_alerts_status ON emergency_alerts(alert_status, created_at DESC) WHERE alert_status IN ('PENDING', 'RESPONDING');
CREATE INDEX idx_emergency_alerts_session ON emergency_alerts(session_id);
CREATE INDEX idx_emergency_alerts_assigned ON emergency_alerts(assigned_ranger_id, alert_status);
CREATE INDEX idx_emergency_alerts_created ON emergency_alerts(created_at DESC);
CREATE INDEX idx_emergency_alerts_point ON emergency_alerts USING GIST (location_point);
CREATE INDEX idx_emergency_alerts_type_date ON emergency_alerts(alert_type, created_at);