package com.beacon.incidents.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class GrpcServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final Server server;
    private final int port;
    private volatile boolean running;

    public GrpcServerLifecycle(IncidentGrpcService incidentService, @Value("${beacon.grpc.port}") int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(incidentService)
                .addService(ProtoReflectionService.newInstance())
                .build();
    }

    @Override
    public void start() {
        try {
            server.start();
            running = true;
            log.info("gRPC server listening on port {}", port);
        } catch (IOException e) {
            throw new IllegalStateException("Could not start gRPC server on port " + port, e);
        }
    }

    @Override
    public void stop() {
        server.shutdown();
        try {
            if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public void awaitTermination() throws InterruptedException {
        server.awaitTermination();
    }
}
