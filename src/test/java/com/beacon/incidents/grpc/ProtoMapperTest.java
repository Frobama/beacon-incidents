package com.beacon.incidents.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beacon.incidents.domain.EntryKind;
import com.beacon.incidents.domain.Incident;
import com.beacon.incidents.domain.IncidentStatus;
import com.beacon.incidents.domain.Severity;
import com.beacon.incidents.domain.TimelineEntry;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProtoMapperTest {

    @Test
    void mapsIncidentToProto() {
        Incident incident = Incident.open("DB down", "checkout", Severity.HIGH);
        TimelineEntry entry = TimelineEntry.of(incident.getId(), EntryKind.NOTE, "hola", "ana");

        com.beacon.incidents.v1.Incident proto = ProtoMapper.incidentToProto(incident, List.of(entry));

        assertEquals(incident.getId().toString(), proto.getId());
        assertEquals("DB down", proto.getTitle());
        assertEquals(com.beacon.common.v1.IncidentStatus.INCIDENT_STATUS_TRIGGERED, proto.getStatus());
        assertEquals(com.beacon.common.v1.Severity.SEVERITY_HIGH, proto.getSeverity());
        assertEquals(1, proto.getTimelineCount());
        assertTrue(proto.getAssigneeId().isEmpty());
        assertTrue(!proto.hasResolvedAt());
    }

    @Test
    void convertsInstantToTimestamp() {
        Instant instant = Instant.ofEpochSecond(1_700_000_000L, 123_000_000);

        var timestamp = ProtoMapper.toTimestamp(instant);

        assertEquals(1_700_000_000L, timestamp.getSeconds());
        assertEquals(123_000_000, timestamp.getNanos());
    }

    @Test
    void unspecifiedFiltersMapToNull() {
        assertNull(ProtoMapper.severityOrNull(com.beacon.common.v1.Severity.SEVERITY_UNSPECIFIED));
        assertNull(ProtoMapper.statusOrNull(com.beacon.common.v1.IncidentStatus.INCIDENT_STATUS_UNSPECIFIED));
    }

    @Test
    void requiredEnumsRejectUnspecified() {
        assertThrows(IllegalArgumentException.class,
                () -> ProtoMapper.severityRequired(com.beacon.common.v1.Severity.SEVERITY_UNSPECIFIED));
        assertThrows(IllegalArgumentException.class,
                () -> ProtoMapper.statusRequired(com.beacon.common.v1.IncidentStatus.INCIDENT_STATUS_UNSPECIFIED));
    }

    @Test
    void enumsRoundTrip() {
        for (Severity severity : Severity.values()) {
            assertEquals(severity, ProtoMapper.severityRequired(ProtoMapper.severityToProto(severity)));
        }
        for (IncidentStatus status : IncidentStatus.values()) {
            assertEquals(status, ProtoMapper.statusRequired(ProtoMapper.statusToProto(status)));
        }
        for (EntryKind kind : EntryKind.values()) {
            assertEquals(kind, ProtoMapper.kindRequired(ProtoMapper.kindToProto(kind)));
        }
    }
}
