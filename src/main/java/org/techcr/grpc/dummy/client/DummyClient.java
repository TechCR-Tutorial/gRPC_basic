package org.techcr.grpc.dummy.client;


import org.proto.dummy.DummyServiceGrpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DummyClient {

    public static void main(String[] args) {
        System.out.println("gRPC dummy client");

        System.out.println("Creating Dummy Channel. ");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50020).build();

        DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);

        System.out.println("Dummy Channel shutdown");
        channel.shutdown();
    }

}
