package com.beacon.incidents.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beacon.incidents.domain.EntryKind;
import com.beacon.incidents.domain.Incident;
import com.beacon.incidents.domain.IncidentNotFoundException;
import com.beacon.incidents.domain.IncidentStatus;
import com.beacon.incidents.domain.InvalidTransitionException;
import com.beacon.incidents.domain.Severity;
import com.beacon.incidents.domain.TimelineEntry;
import com.beacon.incidents.repository.IncidentRepository;
import com.beacon.incidents.repository.TimelineEntryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    IncidentRepository incidents;

    @Mock
    TimelineEntryRepository timeline;

    IncidentService service;

    @BeforeEach
    void setUp() {
        service = new IncidentService(incidents, timeline);
        lenient().when(incidents.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(timeline.save(any(TimelineEntry.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createSavesIncidentAndLeavesTimelineEntry() {
        IncidentView view = service.create("  DB down  ", "checkout", Severity.HIGH, "ana");

        assertEquals("DB down", view.incident().getTitle());
        assertEquals(IncidentStatus.TRIGGERED, view.incident().getStatus());
        assertNotNull(view.incident().getId());

        ArgumentCaptor<TimelineEntry> captor = ArgumentCaptor.forClass(TimelineEntry.class);
        verify(timeline).save(captor.capture());
        assertEquals(EntryKind.STATUS_CHANGED, captor.getValue().getKind());
        assertEquals("ana", captor.getValue().getActor());
    }

    @Test
    void createUsesSystemActorWhenBlank() {
        service.create("t", "svc", Severity.LOW, " ");

        ArgumentCaptor<TimelineEntry> captor = ArgumentCaptor.forClass(TimelineEntry.class);
        verify(timeline).save(captor.capture());
        assertEquals("system", captor.getValue().getActor());
    }

    @Test
    void createRejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> service.create(" ", "svc", Severity.LOW, "ana"));
        verify(incidents, never()).save(any());
    }

    @Test
    void acknowledgeSetsAssigneeAndLogsTransition() {
        Incident incident = Incident.open("t", "svc", Severity.HIGH);
        when(incidents.findById(incident.getId())).thenReturn(Optional.of(incident));

        IncidentView view = service.updateStatus(incident.getId(), IncidentStatus.ACKNOWLEDGED, "ana");

        assertEquals(IncidentStatus.ACKNOWLEDGED, view.incident().getStatus());
        assertEquals("ana", view.incident().getAssigneeId());

        ArgumentCaptor<TimelineEntry> captor = ArgumentCaptor.forClass(TimelineEntry.class);
        verify(timeline).save(captor.capture());
        assertEquals(EntryKind.STATUS_CHANGED, captor.getValue().getKind());
    }

    @Test
    void resolveSetsResolvedAt() {
        Incident incident = Incident.open("t", "svc", Severity.HIGH);
        when(incidents.findById(incident.getId())).thenReturn(Optional.of(incident));

        IncidentView view = service.updateStatus(incident.getId(), IncidentStatus.RESOLVED, "ana");

        assertNotNull(view.incident().getResolvedAt());
    }

    @Test
    void invalidTransitionIsRejectedAndNothingIsLogged() {
        Incident incident = Incident.open("t", "svc", Severity.HIGH);
        when(incidents.findById(incident.getId())).thenReturn(Optional.of(incident));

        assertThrows(InvalidTransitionException.class,
                () -> service.updateStatus(incident.getId(), IncidentStatus.POSTMORTEM, "ana"));
        verify(timeline, never()).save(any());
    }

    @Test
    void getThrowsWhenIncidentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(incidents.findById(id)).thenReturn(Optional.empty());

        assertThrows(IncidentNotFoundException.class, () -> service.get(id));
    }

    @Test
    void addTimelineEntryRequiresExistingIncident() {
        UUID id = UUID.randomUUID();
        when(incidents.findById(id)).thenReturn(Optional.empty());

        assertThrows(IncidentNotFoundException.class,
                () -> service.addTimelineEntry(id, EntryKind.NOTE, "hola", "ana"));
    }

    @Test
    void listUsesDefaultPageSizeWhenNotProvided() {
        Incident incident = Incident.open("t", "svc", Severity.LOW);
        when(incidents.findAll(ArgumentMatchers.<Specification<Incident>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(incident), PageRequest.of(0, 20), 1));

        IncidentPage page = service.list(null, null, null, 0, 0);

        assertEquals(1, page.items().size());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(incidents).findAll(ArgumentMatchers.<Specification<Incident>>any(), captor.capture());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void listCapsPageSize() {
        when(incidents.findAll(ArgumentMatchers.<Specification<Incident>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.list(null, null, null, 10_000, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(incidents).findAll(ArgumentMatchers.<Specification<Incident>>any(), captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }
}
