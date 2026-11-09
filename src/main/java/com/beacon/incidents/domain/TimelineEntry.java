package com.beacon.incidents.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "timeline_entries")
public class TimelineEntry {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EntryKind kind;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TimelineEntry() {
        // requerido por JPA
    }

    public static TimelineEntry of(UUID incidentId, EntryKind kind, String message, String actor) {
        TimelineEntry entry = new TimelineEntry();
        entry.id = UUID.randomUUID();
        entry.incidentId = incidentId;
        entry.kind = kind;
        entry.message = message;
        entry.actor = actor;
        entry.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        return entry;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public EntryKind getKind() {
        return kind;
    }

    public String getMessage() {
        return message;
    }

    public String getActor() {
        return actor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
