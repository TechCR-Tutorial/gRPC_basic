package org.techcr.grpc.greet.server;

import java.io.File;
import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GreetServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Greet Server...");

        //Unsecure server
//        Server server = ServerBuilder.forPort(50021)
//            .addService(new GreetServiceImpl())
//            .build();

        // secure server
        Server server = ServerBuilder.forPort(50021)
                .addService(new GreetServiceImpl())
                .useTransportSecurity(
                        new File("ssl/server.crt"),
                        new File("ssl/server.pem")
                )
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Greeting server shutdown. ");
            server.shutdown();
        }));

        server.awaitTermination();
    }

}
