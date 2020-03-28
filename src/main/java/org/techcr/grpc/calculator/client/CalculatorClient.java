package org.techcr.grpc.calculator.client;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.proto.calculator.AverageCalculatorRequest;
import org.proto.calculator.AverageCalculatorResponse;
import org.proto.calculator.Calculator;
import org.proto.calculator.CalculatorRequest;
import org.proto.calculator.CalculatorResponse;
import org.proto.calculator.CalculatorServiceGrpc;
import org.proto.calculator.MaxNumberCalculatorRequest;
import org.proto.calculator.PrimeNumberDecompositionRequest;
import org.proto.calculator.SquareRootRequest;
import org.proto.calculator.SquareRootResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class CalculatorClient {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Calculator Client");
        CalculatorClient client = new CalculatorClient();
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50022)
            .usePlaintext()
            .build();

        client.checkAverage(channel);
        client.addNumber(channel);
        client.primeNumberDecomposition(channel);
        client.findMaxNumber(channel);
        client.checkError(channel);
        channel.shutdown();
    }

    private void checkError(ManagedChannel channel) {
        System.out.println("Calculator checkError");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient = CalculatorServiceGrpc.newBlockingStub(channel);
        try {
            SquareRootResponse response = syncClient.squareRoot(SquareRootRequest.newBuilder()
                .setBaseNumber(-1)
                .build());
            System.out.println("Square root response: " + response.getResult());
        } catch (StatusRuntimeException e) {
            System.out.println("Exception occurd while sqaure root: " + e.getStatus().getDescription());
            e.printStackTrace();
        }
    }

    private void findMaxNumber(ManagedChannel channel) throws InterruptedException {
        System.out.println("Calculator findMaxNumber");
        CalculatorServiceGrpc.CalculatorServiceStub calculatorStub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<MaxNumberCalculatorRequest> maxNumberRequest = calculatorStub
            .findMaxNumber(new StreamObserver<CalculatorResponse>() {
                @Override
                public void onNext(CalculatorResponse value) {
                    System.out.println("Response value: " + value.getResult());
                }

                @Override
                public void onError(Throwable t) {
                    //on error
                }

                @Override
                public void onCompleted() {
                    latch.countDown();
                }
            });

        Arrays.asList(1, 5, 3, 6, 2, 20).forEach(number ->
            maxNumberRequest.onNext(MaxNumberCalculatorRequest.newBuilder()
                .setBaseNumber(number)
                .build())
        );
        maxNumberRequest.onCompleted();
        latch.await(1, TimeUnit.SECONDS);


    }

    public void checkAverage(ManagedChannel channel) throws InterruptedException {
        System.out.println("Calculator addNumber");
        CountDownLatch latch = new CountDownLatch(1);
        CalculatorServiceGrpc.CalculatorServiceStub calculatorAsyncClient = CalculatorServiceGrpc.newStub(channel);
        StreamObserver<AverageCalculatorRequest> requestObserver = calculatorAsyncClient
            .calculateAverage(new StreamObserver<AverageCalculatorResponse>() {
                @Override
                public void onNext(AverageCalculatorResponse value) {
                    System.out.println("Average is: " + value.getResult());
                }

                @Override
                public void onError(Throwable t) {
                    //Error
                }

                @Override
                public void onCompleted() {
                    System.out.println("Calculator Response Completed");
                    latch.countDown();
                }
            });
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int randomNumber = random.nextInt(100);
            requestObserver.onNext(AverageCalculatorRequest.newBuilder()
                .setBaseNumber(randomNumber)
                .build());
        }
        requestObserver.onCompleted();
        latch.await();
    }

    public void addNumber(ManagedChannel channel) {
        System.out.println("Calculator addNumber");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc
            .newBlockingStub(channel);

        Calculator calculator = Calculator.newBuilder()
            .setNumberOne(10)
            .setNumberTwo(20)
            .build();
        CalculatorRequest request = CalculatorRequest.newBuilder()
            .setCalculator(calculator)
            .build();
        CalculatorResponse response = calculatorClient.plusCalculate(request);
        System.out.println("Result: " + response.getResult());
    }

    public void primeNumberDecomposition(ManagedChannel channel) {
        System.out.println("Calculator primeNumberDecomposition");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc
            .newBlockingStub(channel);
        int baseNumber = 5000;
        calculatorClient
            .primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder().setBaseNumber(baseNumber).build())
            .forEachRemaining(dividerResponse -> System.out.println("Prime Number: " + dividerResponse));

    }
}
