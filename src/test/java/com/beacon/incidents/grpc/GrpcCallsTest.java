package com.beacon.incidents.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beacon.incidents.domain.Incident;
import com.beacon.incidents.domain.IncidentNotFoundException;
import com.beacon.incidents.domain.IncidentStatus;
import com.beacon.incidents.domain.InvalidTransitionException;
import io.grpc.Status;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class GrpcCallsTest {

    @Test
    void mapsDomainErrorsToGrpcStatusCodes() {
        assertEquals(Status.Code.NOT_FOUND,
                GrpcCalls.toStatus(new IncidentNotFoundException(UUID.randomUUID())).getCode());
        assertEquals(Status.Code.FAILED_PRECONDITION,
                GrpcCalls.toStatus(new InvalidTransitionException(
                        IncidentStatus.TRIGGERED, IncidentStatus.POSTMORTEM)).getCode());
        assertEquals(Status.Code.INVALID_ARGUMENT,
                GrpcCalls.toStatus(new IllegalArgumentException("bad")).getCode());
        assertEquals(Status.Code.ABORTED,
                GrpcCalls.toStatus(new ObjectOptimisticLockingFailureException(Incident.class, "id")).getCode());
        assertEquals(Status.Code.INTERNAL,
                GrpcCalls.toStatus(new RuntimeException("boom")).getCode());
    }
}
