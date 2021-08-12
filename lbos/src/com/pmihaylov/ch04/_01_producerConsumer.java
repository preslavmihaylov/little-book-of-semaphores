package com.pmihaylov.ch04;

import java.util.concurrent.*;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _01_producerConsumer {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(5);
        ExecutorService producers = Executors.newFixedThreadPool(5);
        ExecutorService consumers = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            producers.submit(() -> handleInterruptedException(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Producer is producing some work...");
                    queue.add(() -> System.out.println("consumer is doing some work..."));
                    Thread.sleep(500);
                }

                System.out.println("Producer was interrupted. Exiting...");
            }));
        }

        for (int i = 0; i < 2; i++) {
            consumers.submit(() -> handleInterruptedException(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    queue.take().run();
                }

                System.out.println("Consumer was interrupted. Exiting...");
            }));
        }

        Thread.sleep(5000);
        producers.shutdownNow();
        producers.awaitTermination(2, TimeUnit.SECONDS);

        consumers.shutdownNow();
        consumers.awaitTermination(2, TimeUnit.SECONDS);
    }
}
