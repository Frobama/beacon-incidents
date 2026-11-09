CREATE TABLE incidents (
    id          UUID         PRIMARY KEY,
    version     BIGINT       NOT NULL,
    title       VARCHAR(255) NOT NULL,
    service     VARCHAR(100) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    severity    VARCHAR(20)  NOT NULL,
    assignee_id VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    resolved_at TIMESTAMPTZ
);

CREATE INDEX idx_incidents_status     ON incidents (status);
CREATE INDEX idx_incidents_created_at ON incidents (created_at DESC);

-- Alertas asociadas (se usará desde la Fase 2)
CREATE TABLE incident_alerts (
    incident_id UUID         NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    alert_id    VARCHAR(100) NOT NULL,
    PRIMARY KEY (incident_id, alert_id)
);

CREATE TABLE timeline_entries (
    id          UUID          PRIMARY KEY,
    version     BIGINT        NOT NULL,
    incident_id UUID          NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    kind        VARCHAR(40)   NOT NULL,
    message     VARCHAR(2000) NOT NULL,
    actor       VARCHAR(100)  NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_timeline_incident ON timeline_entries (incident_id, created_at);
