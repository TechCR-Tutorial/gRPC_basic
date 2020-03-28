package org.techcr.grpc.calculator.server;

import java.util.ArrayList;
import java.util.List;

import org.proto.calculator.AverageCalculatorRequest;
import org.proto.calculator.AverageCalculatorResponse;
import org.proto.calculator.Calculator;
import org.proto.calculator.CalculatorRequest;
import org.proto.calculator.CalculatorResponse;
import org.proto.calculator.CalculatorServiceGrpc;
import org.proto.calculator.MaxNumberCalculatorRequest;
import org.proto.calculator.PrimeNumberDecompositionRequest;
import org.proto.calculator.PrimeNumberDecompositionResponse;
import org.proto.calculator.SquareRootRequest;
import org.proto.calculator.SquareRootResponse;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void plusCalculate(CalculatorRequest request, StreamObserver<CalculatorResponse> responseObserver) {
        Calculator calculator = request.getCalculator();

        int result = calculator.getNumberOne() + calculator.getNumberTwo();
        System.out.println("Calculator result: (" + calculator + ") = " + result);

        CalculatorResponse response = CalculatorResponse.newBuilder()
            .setResult(result)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request,
        StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        System.out.println("Prime Number Decomposition");

        int primeNumber = 2;
        int baseNumber = request.getBaseNumber();
        while (baseNumber > 1) {
            if (baseNumber % primeNumber == 0) {
                baseNumber = baseNumber / primeNumber;
                responseObserver.onNext(
                    PrimeNumberDecompositionResponse.newBuilder()
                        .setResult(primeNumber)
                        .build());
            }
            else {
                primeNumber++;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<AverageCalculatorRequest> calculateAverage(StreamObserver<AverageCalculatorResponse> responseObserver) {

        System.out.println("Calculator Calculate Average funtion");
        return new StreamObserver<AverageCalculatorRequest>() {
            int sum = 0;
            int count = 0;
            @Override
            public void onNext(AverageCalculatorRequest value) {
                System.out.println("Getting base value " + value.getBaseNumber());
                sum += value.getBaseNumber();
                count++;
            }

            @Override
            public void onError(Throwable t) {
                //on error
            }

            @Override
            public void onCompleted() {
                double average = (double) sum / count;
                responseObserver.onNext(AverageCalculatorResponse.newBuilder()
                    .setResult(average)
                    .build());
                responseObserver.onCompleted();
                System.out.println("Calculator Request completed-average: " + average);
            }
        };
    }

    @Override
    public StreamObserver<MaxNumberCalculatorRequest> findMaxNumber(StreamObserver<CalculatorResponse> responseObserver) {
        return new StreamObserver<MaxNumberCalculatorRequest>() {
            int max = 0;
            @Override
            public void onNext(MaxNumberCalculatorRequest value) {
                int requestNumber = value.getBaseNumber();
                System.out.println("Getting request number: " + requestNumber);
                if (max < requestNumber) {
                    System.out.println("setting max value: " + requestNumber);
                    max = requestNumber;
                    responseObserver.onNext(CalculatorResponse.newBuilder()
                        .setResult(requestNumber)
                        .build());
                }
            }

            @Override
            public void onError(Throwable t) {
                //on error
            }

            @Override
            public void onCompleted() {
                System.out.println("Request complete occur");
                responseObserver.onNext(CalculatorResponse.newBuilder()
                    .setResult(max)
                    .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {

        Integer number = request.getBaseNumber();
        if (number >= 0) {
            double squareRoot = Math.sqrt(number);
            responseObserver.onNext(
                SquareRootResponse.newBuilder()
                    .setResult(squareRoot)
                    .build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("The request number is not positive")
                    .augmentDescription("The number requested: " + request.getBaseNumber())
                    .asRuntimeException()
            );
        }
    }
}
