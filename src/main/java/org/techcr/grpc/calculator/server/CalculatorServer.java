package org.techcr.grpc.calculator.server;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class CalculatorServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Calculator Server");
        System.out.println("Process ID: " + getPID());
        Server server = ServerBuilder.forPort(50022)
            .addService(new CalculatorServiceImpl())
            .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Calculator server shutdown");
            server.shutdown();
        }));
        server.start();
        server.awaitTermination();
    }

    public static long getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        if (processName != null && processName.length() > 0) {
            try {
                return Long.parseLong(processName.split("@")[0]);
            }
            catch (Exception e) {
                return 0;
            }
        }

        return 0;
    }

}
