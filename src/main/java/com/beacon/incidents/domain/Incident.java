package com.beacon.incidents.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 100)
    private String service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(name = "assignee_id", length = 100)
    private String assigneeId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "incident_alerts", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "alert_id", nullable = false, length = 100)
    private Set<String> alertIds = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Incident() {
        // requerido por JPA
    }

    public static Incident open(String title, String service, Severity severity) {
        Incident incident = new Incident();
        Instant now = now();
        incident.id = UUID.randomUUID();
        incident.title = title;
        incident.service = service;
        incident.severity = severity;
        incident.status = IncidentStatus.TRIGGERED;
        incident.createdAt = now;
        incident.updatedAt = now;
        return incident;
    }

    public void transitionTo(IncidentStatus next, String actor) {
        StatusTransitions.validate(status, next);
        this.status = next;
        this.updatedAt = now();
        if (next == IncidentStatus.RESOLVED) {
            this.resolvedAt = this.updatedAt;
        }
        if (next == IncidentStatus.ACKNOWLEDGED && assigneeId == null && actor != null && !actor.isBlank()) {
            this.assigneeId = actor;
        }
    }

    private static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getService() {
        return service;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public Set<String> getAlertIds() {
        return alertIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
