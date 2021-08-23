package com.pmihaylov.ch05;

import java.util.concurrent.*;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _08_rollerCoaster {
    private static final int SEATS_CNT = 4;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        Semaphore waitingPassengersQueue = new Semaphore(0);
        CyclicBarrier carLoadedBarrier = new CyclicBarrier(SEATS_CNT+1);
        CyclicBarrier carUnloadedBarrier = new CyclicBarrier(SEATS_CNT+1);

        // car
        exec.submit(() -> handleInterruptedException(() -> {
            while (true) {
                waitingPassengersQueue.release(SEATS_CNT);
                System.out.println("Car is waiting for " + SEATS_CNT + " passengers...");
                carLoadedBarrier.await();
                System.out.println("All passengers have been loaded...");
                System.out.println("The car is driving the passengers...");
                Thread.sleep(4000);
                carUnloadedBarrier.await();
                System.out.println("All passengers have been unloaded...");
            }
        }));

        // passengers
        for (int i = 0; i < 10; i++) {
            final int passengerID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 90) {
                        System.out.printf("Passenger #%d is waiting for the car...\n", passengerID);
                        waitingPassengersQueue.acquire();
                        System.out.printf("Passenger #%d is loaded in the car...\n", passengerID);
                        carLoadedBarrier.await();
                        carUnloadedBarrier.await();
                        System.out.printf("Passenger #%d has been unloaded...\n", passengerID);
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        Thread.sleep(20000);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }
}
