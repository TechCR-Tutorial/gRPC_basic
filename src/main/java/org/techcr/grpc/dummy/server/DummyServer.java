package org.techcr.grpc.dummy.server;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class DummyServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("gRPC Dummy Server.");

        Server server = ServerBuilder.forPort(50020).build();

        System.out.println("Dummy Server Started");
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Dummy Server going to shutdown");
            server.shutdown();
            System.out.println("Dummy Server successfully shutdown");
        }));

        server.awaitTermination();
    }

}
