package com.beacon.incidents.domain;

public class InvalidTransitionException extends RuntimeException {

    public InvalidTransitionException(IncidentStatus from, IncidentStatus to) {
        super("Invalid status transition: " + from + " -> " + to);
    }
}
