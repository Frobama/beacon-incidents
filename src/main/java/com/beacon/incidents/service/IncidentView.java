package com.beacon.incidents.service;

import com.beacon.incidents.domain.Incident;
import com.beacon.incidents.domain.TimelineEntry;
import java.util.List;

public record IncidentView(Incident incident, List<TimelineEntry> timeline) {
}
