package org.techcr.grpc.greet.server;

import org.proto.greet.GreetRequest;
import org.proto.greet.GreetResponse;
import org.proto.greet.GreetServiceGrpc;
import org.proto.greet.Greeting;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    /**
     * Unary API
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Greeting greet = request.getGreeting();
        String firstName = greet.getFirstName();
        String lastName = greet.getLastName();
        System.out.println("Request greeting for: " + firstName + " " + lastName);
        String result = "Hello: " + firstName + " " + lastName;

        GreetResponse response = GreetResponse.newBuilder()
            .setResult(result)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Sever Stream API
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void greetManyTimes(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Greeting greeting = request.getGreeting();

        try {
            for (int i = 0; i < 10; i++) {
                String result = "Greet many times to: " + greeting.getFirstName() + " " + greeting.getLastName()
                    + " greet number: " + i;
                GreetResponse response = GreetResponse.newBuilder()
                    .setResult(result)
                    .build();
                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    /**
     * Client Stream API
     *
     * @param responseObserver
     * @return
     */
    @Override
    public StreamObserver<GreetRequest> longGreet(StreamObserver<GreetResponse> responseObserver) {
        return new StreamObserver<GreetRequest>() {

            String result = "";

            @Override
            public void onNext(GreetRequest value) {
                Greeting greeting = value.getGreeting();
                System.out.println("Request onNext: " + greeting.getFirstName() + " " + greeting.getLastName());
                result += "Hello: " + greeting.getFirstName() + " " + greeting.getLastName() + "! ";
            }

            @Override
            public void onError(Throwable t) {
                //on Error
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                    GreetResponse.newBuilder()
                        .setResult(result)
                        .build()
                );
                responseObserver.onCompleted();
                System.out.println("Response observer completed");
            }
        };
    }

    /**
     * Bi-Directional stream API
     *
     * @param responseObserver
     * @return
     */
    @Override
    public StreamObserver<GreetRequest> greetEveryone(StreamObserver<GreetResponse> responseObserver) {
        System.out.println("Greeting Bi-Direct Stream API");
        return new StreamObserver<GreetRequest>() {
            @Override
            public void onNext(GreetRequest value) {
                Greeting greeting = value.getGreeting();
                String result = "Hello " + greeting.getFirstName() + " " + greeting.getLastName() + "!";
                System.out.println(result);
                responseObserver.onNext(GreetResponse.newBuilder()
                    .setResult(result)
                    .build());
            }

            @Override
            public void onError(Throwable t) {
                //Error
            }

            @Override
            public void onCompleted() {
                System.out.println("Request Completed");
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void greetWithDeadline(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Greeting greeting = request.getGreeting();

        Context context = Context.current();
        try {
            System.out.println("Greet request for:" + greeting.getFirstName());
            for (int i = 0; i < 3; i++) {
                if (!context.isCancelled()) {
                    System.out.println("Sleep for 100ms for time: " + i);
                    Thread.sleep(100);
                } else {
                    return;
                }
            }
            System.out.println("Wait Completed: Request not cancelled. ");
            responseObserver.onNext(
                GreetResponse.newBuilder()
                    .setResult("Hello: " + greeting.getFirstName() + " " + greeting.getLastName()
                        + "you are lucky. request not cancelled. ")
                    .build());
            responseObserver.onCompleted();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
