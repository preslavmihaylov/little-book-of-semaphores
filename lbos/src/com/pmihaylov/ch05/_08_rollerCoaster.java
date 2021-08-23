package com.pmihaylov.ch05;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _08_rollerCoaster {
    private static final int SEATS_CNT = 4;
    private static final int CARS_CNT = 4;
    private static class CarData {
        public int id;
        public CyclicBarrier carLoadedBarrier = new CyclicBarrier(SEATS_CNT+1);
        public CyclicBarrier carUnloadedBarrier = new CyclicBarrier(SEATS_CNT+1);

        public CarData(int id) {
            this.id = id;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        Semaphore waitingPassengersQueue = new Semaphore(0);
        AtomicReference<CarData> waitingCar = new AtomicReference<>();
        Lock carLock = new ReentrantLock(true);

        // cars
        for (int i = 0; i < CARS_CNT; i++) {
            final int carID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                CarData thisCarData = new CarData(carID);
                while (true) {
                    System.out.printf("Car #%d has arrived at the entrance...\n", carID);
                    carLock.lock();
                    try {
                        waitingCar.set(thisCarData);
                        waitingPassengersQueue.release(SEATS_CNT);

                        System.out.printf("Car #%d is waiting for %d passengers...\n", carID, SEATS_CNT);
                        thisCarData.carLoadedBarrier.await();
                        System.out.printf("Car #%d has loaded all passengers...\n", carID);
                    } finally {
                        carLock.unlock();
                    }

                    System.out.printf("Car #%d is driving the passengers...\n", carID);
                    Thread.sleep(3000 + ThreadLocalRandom.current().nextInt() % 3000);

                    System.out.printf("Car #%d has finished the ride & arrived at the entrance...\n", carID);

                    carLock.lock();
                    try {
                        thisCarData.carUnloadedBarrier.await();
                        System.out.printf("Car #%d has unloaded all passengers...\n", carID);
                    } finally {
                        carLock.unlock();
                    }
                }
            }));
        }

        // passengers
        for (int i = 0; i < 20; i++) {
            final int passengerID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 90) {
                        System.out.printf("Passenger #%d is waiting for the car...\n", passengerID);
                        waitingPassengersQueue.acquire();

                        CarData carData = waitingCar.get();
                        System.out.printf("Passenger #%d is loaded in car #%d...\n", passengerID, carData.id);
                        carData.carLoadedBarrier.await();
                        carData.carUnloadedBarrier.await();
                        System.out.printf("Passenger #%d has been unloaded from car #%d...\n", passengerID, carData.id);
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        Thread.sleep(30000);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }
}
