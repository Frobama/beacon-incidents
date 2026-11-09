package com.beacon.incidents.service;

import com.beacon.incidents.domain.EntryKind;
import com.beacon.incidents.domain.Incident;
import com.beacon.incidents.domain.IncidentNotFoundException;
import com.beacon.incidents.domain.IncidentStatus;
import com.beacon.incidents.domain.Severity;
import com.beacon.incidents.domain.TimelineEntry;
import com.beacon.incidents.repository.IncidentRepository;
import com.beacon.incidents.repository.TimelineEntryRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;
    private static final String SYSTEM_ACTOR = "system";

    private final IncidentRepository incidents;
    private final TimelineEntryRepository timeline;

    public IncidentService(IncidentRepository incidents, TimelineEntryRepository timeline) {
        this.incidents = incidents;
        this.timeline = timeline;
    }

    @Transactional
    public IncidentView create(String title, String service, Severity severity, String actor) {
        requireText(title, "title");
        requireText(service, "service");
        if (severity == null) {
            throw new IllegalArgumentException("severity is required");
        }
        Incident incident = incidents.save(Incident.open(title.trim(), service.trim(), severity));
        addEntry(incident.getId(), EntryKind.STATUS_CHANGED,
                "Incidente creado con severidad " + severity, actor);
        return view(incident);
    }

    @Transactional(readOnly = true)
    public IncidentView get(UUID id) {
        return view(find(id));
    }

    @Transactional(readOnly = true)
    public IncidentPage list(IncidentStatus status, Severity severity, String service,
                             int pageSize, int pageIndex) {
        int size = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        Page<Incident> page = incidents.findAll(
                IncidentSpecs.filter(status, severity, service),
                PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new IncidentPage(page.getContent(), page.hasNext());
    }

    @Transactional
    public IncidentView updateStatus(UUID id, IncidentStatus next, String actor) {
        if (next == null) {
            throw new IllegalArgumentException("status is required");
        }
        Incident incident = find(id);
        IncidentStatus previous = incident.getStatus();
        incident.transitionTo(next, actor);
        addEntry(id, EntryKind.STATUS_CHANGED, "Estado: " + previous + " → " + next, actor);
        return view(incident);
    }

    @Transactional
    public TimelineEntry addTimelineEntry(UUID incidentId, EntryKind kind, String message, String actor) {
        requireText(message, "message");
        if (kind == null) {
            throw new IllegalArgumentException("kind is required");
        }
        find(incidentId);
        return addEntry(incidentId, kind, message.trim(), actor);
    }

    private Incident find(UUID id) {
        return incidents.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
    }

    private TimelineEntry addEntry(UUID incidentId, EntryKind kind, String message, String actor) {
        String who = (actor == null || actor.isBlank()) ? SYSTEM_ACTOR : actor.trim();
        return timeline.save(TimelineEntry.of(incidentId, kind, message, who));
    }

    private IncidentView view(Incident incident) {
        return new IncidentView(incident, timeline.findByIncidentIdOrderByCreatedAtAsc(incident.getId()));
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
