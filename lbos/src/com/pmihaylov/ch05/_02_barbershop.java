package com.pmihaylov.ch05;

import java.util.concurrent.*;

import static com.pmihaylov.common.Utils.handleInterruptedException;

// Also implements problem 3 - FIFO barbershop by using a "fair" semaphore
public class _02_barbershop {
    private static final int SEATS_CNT = 5;
    private static final int SLEEP_OFFSET = 1000;
    private static final int HAIRCUT_DURATION = 500;

    private static Integer freeChairs = SEATS_CNT;

    public static void main(String[] args) throws InterruptedException {
        // The second argument ensures a "fair" semaphore
        Semaphore cutHairStartedSig = new Semaphore(0, true);
        Semaphore cutHairFinishedSig = new Semaphore(0, true);
        ExecutorService exec = Executors.newCachedThreadPool();

        // barber
        exec.submit(() -> handleInterruptedException(() -> {
            while (true) {
                boolean pendingHaircut = false;
                synchronized (_02_barbershop.class) {
                    if (freeChairs < SEATS_CNT) {
                        pendingHaircut = true;
                    }
                }

                if (!pendingHaircut) {
                    System.out.println("[B] Barber goes to sleep...");
                    Thread.sleep(SLEEP_OFFSET + 1000);
                } else {
                    cutHairStartedSig.release();
                    System.out.println("[B] Barber is cutting hair...");
                    Thread.sleep(SLEEP_OFFSET + HAIRCUT_DURATION + (ThreadLocalRandom.current().nextInt() % 1000));
                    cutHairFinishedSig.release();
                    synchronized (_02_barbershop.class) {
                        freeChairs++;
                    }
                }
            }
        }));

        // clients
        for (int i = 0; i < 6; i++) {
            final int customerID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 50) {
                        Thread.sleep(SLEEP_OFFSET + 1000 + (ThreadLocalRandom.current().nextInt() % 5000));
                        continue;
                    }

                    boolean pendingHaircut = false;
                    synchronized (_02_barbershop.class) {
                        if (freeChairs > 0) {
                            freeChairs--;
                            System.out.printf("[C] Client %d takes a free chair... Remaining seats=%d\n", customerID, freeChairs);
                            pendingHaircut = true;
                        }
                    }

                    if (pendingHaircut) {
                        cutHairStartedSig.acquire();
                        System.out.printf("[C] Client %d is getting their hair cut...\n", customerID);
                        cutHairFinishedSig.acquire();
                    } else {
                        System.out.printf("[C] Client %d leaves as there are no free chairs...\n", customerID);
                    }

                    Thread.sleep(SLEEP_OFFSET + 2000 + (ThreadLocalRandom.current().nextInt() % 1000));
                }
            }));
        }

        Thread.sleep(20000);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }
}
