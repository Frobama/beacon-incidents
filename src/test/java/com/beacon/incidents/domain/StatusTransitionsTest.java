package com.beacon.incidents.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatusTransitionsTest {

    @ParameterizedTest
    @CsvSource({
            "TRIGGERED,ACKNOWLEDGED",
            "TRIGGERED,RESOLVED",
            "ACKNOWLEDGED,MITIGATING",
            "ACKNOWLEDGED,RESOLVED",
            "MITIGATING,RESOLVED",
            "RESOLVED,POSTMORTEM"
    })
    void allowsValidTransitions(IncidentStatus from, IncidentStatus to) {
        assertTrue(StatusTransitions.isAllowed(from, to));
        assertDoesNotThrow(() -> StatusTransitions.validate(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "TRIGGERED,MITIGATING",
            "TRIGGERED,POSTMORTEM",
            "ACKNOWLEDGED,TRIGGERED",
            "MITIGATING,ACKNOWLEDGED",
            "RESOLVED,TRIGGERED",
            "RESOLVED,RESOLVED",
            "POSTMORTEM,RESOLVED"
    })
    void rejectsInvalidTransitions(IncidentStatus from, IncidentStatus to) {
        assertFalse(StatusTransitions.isAllowed(from, to));
        assertThrows(InvalidTransitionException.class, () -> StatusTransitions.validate(from, to));
    }
}
