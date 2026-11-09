package com.beacon.incidents.domain;

import static com.beacon.incidents.domain.IncidentStatus.ACKNOWLEDGED;
import static com.beacon.incidents.domain.IncidentStatus.MITIGATING;
import static com.beacon.incidents.domain.IncidentStatus.POSTMORTEM;
import static com.beacon.incidents.domain.IncidentStatus.RESOLVED;
import static com.beacon.incidents.domain.IncidentStatus.TRIGGERED;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class StatusTransitions {

    private static final Map<IncidentStatus, Set<IncidentStatus>> ALLOWED = new EnumMap<>(IncidentStatus.class);

    static {
        ALLOWED.put(TRIGGERED, EnumSet.of(ACKNOWLEDGED, RESOLVED));
        ALLOWED.put(ACKNOWLEDGED, EnumSet.of(MITIGATING, RESOLVED));
        ALLOWED.put(MITIGATING, EnumSet.of(RESOLVED));
        ALLOWED.put(RESOLVED, EnumSet.of(POSTMORTEM));
        ALLOWED.put(POSTMORTEM, EnumSet.noneOf(IncidentStatus.class));
    }

    private StatusTransitions() {
    }

    public static boolean isAllowed(IncidentStatus from, IncidentStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public static void validate(IncidentStatus from, IncidentStatus to) {
        if (!isAllowed(from, to)) {
            throw new InvalidTransitionException(from, to);
        }
    }
}
