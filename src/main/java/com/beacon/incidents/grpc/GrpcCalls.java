package com.beacon.incidents.grpc;

import com.beacon.incidents.domain.IncidentNotFoundException;
import com.beacon.incidents.domain.InvalidTransitionException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

/** Ejecuta una llamada gRPC y traduce las excepciones de dominio a códigos de estado. */
final class GrpcCalls {

    private static final Logger log = LoggerFactory.getLogger(GrpcCalls.class);

    private GrpcCalls() {
    }

    static <T> void handle(StreamObserver<T> observer, Supplier<T> action) {
        try {
            observer.onNext(action.get());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(toStatus(e).asRuntimeException());
        }
    }

    static Status toStatus(Exception e) {
        if (e instanceof IncidentNotFoundException) {
            return Status.NOT_FOUND.withDescription(e.getMessage());
        }
        if (e instanceof InvalidTransitionException) {
            return Status.FAILED_PRECONDITION.withDescription(e.getMessage());
        }
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        }
        if (e instanceof OptimisticLockingFailureException) {
            return Status.ABORTED.withDescription("Concurrent update, please retry");
        }
        log.error("Unexpected error in gRPC call", e);
        return Status.INTERNAL.withDescription("Internal error");
    }
}
