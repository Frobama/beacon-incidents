package com.beacon.incidents.grpc;

import com.beacon.incidents.domain.EntryKind;
import com.beacon.incidents.domain.Incident;
import com.beacon.incidents.domain.IncidentStatus;
import com.beacon.incidents.domain.Severity;
import com.beacon.incidents.domain.TimelineEntry;
import com.google.protobuf.Timestamp;
import com.beacon.incidents.v1.TimelineEntryKind;
import java.time.Instant;
import java.util.List;

/** Traduce entre el dominio y los mensajes Protobuf de beacon-contracts. */
public final class ProtoMapper {

    private ProtoMapper() {
    }

    // ---------- Severity ----------

    public static Severity severityOrNull(com.beacon.common.v1.Severity value) {
        return switch (value) {
            case SEVERITY_LOW -> Severity.LOW;
            case SEVERITY_MEDIUM -> Severity.MEDIUM;
            case SEVERITY_HIGH -> Severity.HIGH;
            case SEVERITY_CRITICAL -> Severity.CRITICAL;
            default -> null; // UNSPECIFIED o UNRECOGNIZED
        };
    }

    public static Severity severityRequired(com.beacon.common.v1.Severity value) {
        Severity severity = severityOrNull(value);
        if (severity == null) {
            throw new IllegalArgumentException("severity is required");
        }
        return severity;
    }

    public static com.beacon.common.v1.Severity severityToProto(Severity value) {
        return switch (value) {
            case LOW -> com.beacon.common.v1.Severity.SEVERITY_LOW;
            case MEDIUM -> com.beacon.common.v1.Severity.SEVERITY_MEDIUM;
            case HIGH -> com.beacon.common.v1.Severity.SEVERITY_HIGH;
            case CRITICAL -> com.beacon.common.v1.Severity.SEVERITY_CRITICAL;
        };
    }

    // ---------- IncidentStatus ----------

    public static IncidentStatus statusOrNull(com.beacon.common.v1.IncidentStatus value) {
        return switch (value) {
            case INCIDENT_STATUS_TRIGGERED -> IncidentStatus.TRIGGERED;
            case INCIDENT_STATUS_ACKNOWLEDGED -> IncidentStatus.ACKNOWLEDGED;
            case INCIDENT_STATUS_MITIGATING -> IncidentStatus.MITIGATING;
            case INCIDENT_STATUS_RESOLVED -> IncidentStatus.RESOLVED;
            case INCIDENT_STATUS_POSTMORTEM -> IncidentStatus.POSTMORTEM;
            default -> null; // UNSPECIFIED o UNRECOGNIZED
        };
    }

    public static IncidentStatus statusRequired(com.beacon.common.v1.IncidentStatus value) {
        IncidentStatus status = statusOrNull(value);
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        return status;
    }

    public static com.beacon.common.v1.IncidentStatus statusToProto(IncidentStatus value) {
        return switch (value) {
            case TRIGGERED -> com.beacon.common.v1.IncidentStatus.INCIDENT_STATUS_TRIGGERED;
            case ACKNOWLEDGED -> com.beacon.common.v1.IncidentStatus.INCIDENT_STATUS_ACKNOWLEDGED;
            case MITIGATING -> com.beacon.common.v1.IncidentStatus.INCIDENT_STATUS_MITIGATING;
            case RESOLVED -> com.beacon.common.v1.IncidentStatus.INCIDENT_STATUS_RESOLVED;
            case POSTMORTEM -> com.beacon.common.v1.IncidentStatus.INCIDENT_STATUS_POSTMORTEM;
        };
    }

    // ---------- TimelineEntryKind ----------

    public static EntryKind kindRequired(TimelineEntryKind value) {
        return switch (value) {
            case TIMELINE_ENTRY_KIND_STATUS_CHANGED -> EntryKind.STATUS_CHANGED;
            case TIMELINE_ENTRY_KIND_ALERT_ATTACHED -> EntryKind.ALERT_ATTACHED;
            case TIMELINE_ENTRY_KIND_NOTIFICATION_SENT -> EntryKind.NOTIFICATION_SENT;
            case TIMELINE_ENTRY_KIND_ESCALATED -> EntryKind.ESCALATED;
            case TIMELINE_ENTRY_KIND_RUNBOOK_EXECUTED -> EntryKind.RUNBOOK_EXECUTED;
            case TIMELINE_ENTRY_KIND_MCP_ANALYSIS -> EntryKind.MCP_ANALYSIS;
            case TIMELINE_ENTRY_KIND_NOTE -> EntryKind.NOTE;
            default -> throw new IllegalArgumentException("kind is required");
        };
    }

    public static TimelineEntryKind kindToProto(EntryKind value) {
        return switch (value) {
            case STATUS_CHANGED -> TimelineEntryKind.TIMELINE_ENTRY_KIND_STATUS_CHANGED;
            case ALERT_ATTACHED -> TimelineEntryKind.TIMELINE_ENTRY_KIND_ALERT_ATTACHED;
            case NOTIFICATION_SENT -> TimelineEntryKind.TIMELINE_ENTRY_KIND_NOTIFICATION_SENT;
            case ESCALATED -> TimelineEntryKind.TIMELINE_ENTRY_KIND_ESCALATED;
            case RUNBOOK_EXECUTED -> TimelineEntryKind.TIMELINE_ENTRY_KIND_RUNBOOK_EXECUTED;
            case MCP_ANALYSIS -> TimelineEntryKind.TIMELINE_ENTRY_KIND_MCP_ANALYSIS;
            case NOTE -> TimelineEntryKind.TIMELINE_ENTRY_KIND_NOTE;
        };
    }

    // ---------- Mensajes ----------

    public static com.beacon.incidents.v1.TimelineEntry entryToProto(TimelineEntry entry) {
        return com.beacon.incidents.v1.TimelineEntry.newBuilder()
                .setId(entry.getId().toString())
                .setKind(kindToProto(entry.getKind()))
                .setMessage(entry.getMessage())
                .setActor(entry.getActor())
                .setCreatedAt(toTimestamp(entry.getCreatedAt()))
                .build();
    }

    public static com.beacon.incidents.v1.Incident incidentToProto(Incident incident, List<TimelineEntry> entries) {
        com.beacon.incidents.v1.Incident.Builder builder = com.beacon.incidents.v1.Incident.newBuilder()
                .setId(incident.getId().toString())
                .setTitle(incident.getTitle())
                .setService(incident.getService())
                .setStatus(statusToProto(incident.getStatus()))
                .setSeverity(severityToProto(incident.getSeverity()))
                .addAllAlertIds(incident.getAlertIds())
                .setCreatedAt(toTimestamp(incident.getCreatedAt()))
                .setUpdatedAt(toTimestamp(incident.getUpdatedAt()));
        if (incident.getAssigneeId() != null) {
            builder.setAssigneeId(incident.getAssigneeId());
        }
        if (incident.getResolvedAt() != null) {
            builder.setResolvedAt(toTimestamp(incident.getResolvedAt()));
        }
        entries.forEach(entry -> builder.addTimeline(entryToProto(entry)));
        return builder.build();
    }

    public static Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
