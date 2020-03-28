package org.techcr.grpc.blog.server;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class BlogServer {

    public static void main(String[] args) throws InterruptedException, IOException {
        Server server = ServerBuilder.forPort(50023)
            .addService(new BlogServiceImpl())
            .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Blog server shutdown");
            server.shutdown();
        }));

        System.out.println("Blog server started");
        server.start();

        server.awaitTermination();
    }
}
