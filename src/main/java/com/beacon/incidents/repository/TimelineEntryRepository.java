package com.beacon.incidents.repository;

import com.beacon.incidents.domain.TimelineEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimelineEntryRepository extends JpaRepository<TimelineEntry, UUID> {

    List<TimelineEntry> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
