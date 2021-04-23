package org.techcr.grpc.greet.client;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.proto.greet.GreetRequest;
import org.proto.greet.GreetResponse;
import org.proto.greet.GreetServiceGrpc;
import org.proto.greet.Greeting;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GreetClient {

    public static void main(String[] args) throws InterruptedException, SSLException {
        System.out.println("gRPC Greet Client");
        GreetClient client = new GreetClient();


        //Secure Channel
        ManagedChannel secureChannel = NettyChannelBuilder.forAddress("localhost", 50021)
            .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
            .build();
        client.unaryAPI(secureChannel);
        secureChannel.shutdown();

        //Unsecure channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50021)
            .usePlaintext()
            .build();

        //call unary api
//        client.unaryAPI(channel);

        //call server stream api
        //client.serverStreamAPI(channel);

        //call client stream api
        //client.clientStreamAPI(channel);

        //call bi-directional stream api
        //client.biDirectionalStreamAPI(channel);

        //call greet with deadline
//        client.greetWithDeadline(channel);

        //future stub example
        //client.unaryAPIFuture(channel);

        channel.shutdown();
        System.out.println("Channel shutdowned. ");
    }

    private void greetWithDeadline(ManagedChannel channel) {
        System.out.println("Greet with dead line");
        GreetServiceGrpc.GreetServiceBlockingStub syncGreetClient = GreetServiceGrpc.newBlockingStub(channel);
        try {
            System.out.println("Greet with 500 deadline");
            GreetResponse response = syncGreetClient.withDeadlineAfter(1000, TimeUnit.MILLISECONDS)
                .greetWithDeadline(GreetRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder()
                        .setFirstName("Chamly")
                        .setLastName("Rathnayaka")
                        .build())
                    .build());
            System.out.println("500 Deadline Result: " + response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode().equals(Status.DEADLINE_EXCEEDED.getCode())) {
                System.out.println("Dead line has exceeded");
            } else {
                e.printStackTrace();
            }
        }
        try {
            System.out.println("Greet with 100 deadline");
            GreetResponse response = syncGreetClient.withDeadlineAfter(100, TimeUnit.MILLISECONDS)
                .greetWithDeadline(GreetRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder()
                        .setFirstName("Chamly")
                        .setLastName("Rathnayaka")
                        .build())
                    .build());
            System.out.println("100 Deadline Result: " + response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode().equals(Status.DEADLINE_EXCEEDED.getCode())) {
                System.out.println("Dead line has exceeded: 100ms");
            } else {
                e.printStackTrace();
            }
        }

    }

    public void unaryAPI(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub syncGreetClient = GreetServiceGrpc.newBlockingStub(channel);
        //GreetServiceGrpc.GreetServiceFutureStub futureStub = GreetServiceGrpc.newFutureStub(channel);
        System.out.println("Unary API");
        Greeting greeting = Greeting.newBuilder()
            .setFirstName("Chamly")
            .setLastName("Rathnayaka")
            .build();

        GreetRequest request = GreetRequest.newBuilder()
            .setGreeting(greeting)
            .build();

        GreetResponse response = syncGreetClient.greet(request);
        System.out.println("Unary Greet Result: " + response.getResult());
    }

    public void unaryAPIFuture(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceFutureStub syncGreetClient = GreetServiceGrpc.newFutureStub(channel);
        //GreetServiceGrpc.GreetServiceFutureStub futureStub = GreetServiceGrpc.newFutureStub(channel);
        System.out.println("Unary API");
        Greeting greeting = Greeting.newBuilder()
            .setFirstName("Chamly")
            .setLastName("Rathnayaka")
            .build();

        GreetRequest request = GreetRequest.newBuilder()
            .setGreeting(greeting)
            .build();

        ListenableFuture<GreetResponse> futureResponse = syncGreetClient.greet(request);

        Futures.addCallback(futureResponse, new FutureCallback<GreetResponse>() {
            @Override
            public void onSuccess(GreetResponse result) {
                System.out.println("Unary Greet Future Result: " + result.getResult());
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

    public void serverStreamAPI(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub syncGreetClient = GreetServiceGrpc.newBlockingStub(channel);
        //GreetServiceGrpc.GreetServiceFutureStub futureStub = GreetServiceGrpc.newFutureStub(channel);
        Greeting greeting = Greeting.newBuilder()
            .setFirstName("Chamly")
            .setLastName("Rathnayaka")
            .build();

        GreetRequest request = GreetRequest.newBuilder()
            .setGreeting(greeting)
            .build();

        System.out.println("Server stream result print start:");
        syncGreetClient.greetManyTimes(request).forEachRemaining(
            greetResponse -> System.out.println(greetResponse.getResult()));
        System.out.println("Server stream result print end:");

    }

    private void clientStreamAPI(ManagedChannel channel) throws InterruptedException {
        System.out.println(" __________ Client Stream API");
        CountDownLatch latch = new CountDownLatch(1);
        GreetServiceGrpc.GreetServiceStub greetAsyncClient = GreetServiceGrpc.newStub(channel);

        StreamObserver<GreetRequest> requestObserver = greetAsyncClient.longGreet(new StreamObserver<GreetResponse>() {
            @Override
            public void onNext(GreetResponse value) {
                System.out.println("Calling Greet response at once. ");
                System.out.println("Result: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                // to do
            }

            @Override
            public void onCompleted() {
                System.out.println("Call imediatly after on next call. Server complete send messages");
                latch.countDown();
            }
        });

        //Request one
        System.out.println("Sending Message One");
        requestObserver.onNext(GreetRequest.newBuilder()
            .setGreeting(Greeting.newBuilder()
                .setFirstName("Chamly")
                .setLastName("Rathnayaka")
                .build())
            .build());

        //Request two
        System.out.println("Sending Message Two");
        requestObserver.onNext(GreetRequest.newBuilder()
            .setGreeting(Greeting.newBuilder()
                .setFirstName("Idunil")
                .setLastName("Rathnayaka")
                .build())
            .build());

        //Request three
        System.out.println("Sending Message Three");
        requestObserver.onNext(GreetRequest.newBuilder()
            .setGreeting(Greeting.newBuilder()
                .setFirstName("Chamly Two")
                .setLastName("Rathnayaka")
                .build())
            .build());

        requestObserver.onCompleted();
        latch.await();
    }


    private void biDirectionalStreamAPI(ManagedChannel channel) throws InterruptedException {
        System.out.println(" __________ Bi-Directional Stream API");
        CountDownLatch latch = new CountDownLatch(1);
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        StreamObserver<GreetRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<GreetResponse>() {
            @Override
            public void onNext(GreetResponse value) {
                System.out.println("Response value: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                //on error
            }

            @Override
            public void onCompleted() {
                latch.countDown();
                System.out.println("Response completed...");
            }
        });

        for (int i = 0; i < 100; i++) {
            String firstName = "Chamly " + i;
            System.out.println("Sending name- first name: " + firstName);
            requestObserver.onNext(GreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                    .setFirstName(firstName)
                    .setLastName("Rathnayaka")
                    .build())
                .build());
            Thread.sleep(100);

        }
        requestObserver.onCompleted();
        latch.await(1, TimeUnit.SECONDS);
        System.out.println("Bi-directional completed");

    }


}
