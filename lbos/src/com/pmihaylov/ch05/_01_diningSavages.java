package com.pmihaylov.ch05;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _01_diningSavages {
    private static final int POT_CAPACITY = 5;
    private static Integer pot = 0;

    public static void main(String[] args) throws InterruptedException {
        Semaphore potEmptySig = new Semaphore(0);
        ExecutorService exec = Executors.newCachedThreadPool();

        exec.submit(chefThread(potEmptySig));

        for (int i  = 0; i < 3; i++) {
            exec.submit(savageThread(potEmptySig));
        }

        Thread.sleep(20000);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static Runnable chefThread(Semaphore potEmptySig) {
        return () -> handleInterruptedException( () -> {
            while (true) {
                System.out.println("Chef is going to sleep...");
                potEmptySig.acquire();
                System.out.println("Chef is woken up & filling the pot...");
                synchronized (_01_diningSavages.class) {
                    if (pot <= 0) {
                        pot += POT_CAPACITY;
                    }
                }
            }
        });
    }

    private static Runnable savageThread(Semaphore potEmptySig) {
        return () -> handleInterruptedException(() -> {
            while (true) {
                boolean eating = false;
                synchronized (_01_diningSavages.class) {
                    if (pot > POT_CAPACITY || pot < 0) {
                        System.out.println("[INVARIANT VIOLATED] Pot's value is " + pot);
                    }

                    if (pot > 0) {
                        eating = true;
                        pot--;
                    } else {
                        System.out.println("Pot is empty. Summoning the chef...");
                        potEmptySig.release();
                    }
                }

                if (eating) {
                    System.out.println("Savage is eating...");
                    Thread.sleep(1000);
                } else {
                    Thread.sleep(500);
                }
            }
        });
    }
}
