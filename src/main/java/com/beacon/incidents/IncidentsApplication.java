package com.beacon.incidents;

import com.beacon.incidents.grpc.GrpcServerLifecycle;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IncidentsApplication {

    public static void main(String[] args) throws InterruptedException {
        try (ConfigurableApplicationContext context =
                     SpringApplication.run(IncidentsApplication.class, args)) {
            context.getBean(GrpcServerLifecycle.class).awaitTermination();
        }
    }
}
