package com.beacon.incidents.service;

import com.beacon.incidents.domain.Incident;
import java.util.List;

public record IncidentPage(List<Incident> items, boolean hasNext) {
}
