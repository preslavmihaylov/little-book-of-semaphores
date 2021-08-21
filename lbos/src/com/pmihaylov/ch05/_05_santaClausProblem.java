package com.pmihaylov.ch05;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _05_santaClausProblem {
    private static final String ELVES_IN_TROUBLE_MSG = "elvesInTrouble";
    private static final String REINDEER_ARRIVED_MSG = "reindeerArrived";
    private static final int ELVES_IN_TROUBLE_THRESHOLD = 3;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        BlockingQueue<String> wakeUpSantaMessages = new PriorityBlockingQueue<String>(10, (m1, m2) -> {
            if (m1.equals(REINDEER_ARRIVED_MSG)) {
                return -1;
            } else if (m2.equals(REINDEER_ARRIVED_MSG)) {
                return 1;
            } else {
                return m1.compareTo(m2);
            }
        });
        AtomicInteger blockedElves = new AtomicInteger(0);
        AtomicInteger arrivedReindeer = new AtomicInteger(0);
        Semaphore elvesInTroubleSig = new Semaphore(0, true);
        Semaphore reindeerStartHitchSig = new Semaphore(0);
        Semaphore reindeerEndHitchSig = new Semaphore(0);
        Semaphore elvesTroubleResolvedSig = new Semaphore(0, true);

        // santa claus
        exec.submit(() -> handleInterruptedException(() -> {
            while (true) {
                System.out.println("Santa is going to sleep...");
                String msg = wakeUpSantaMessages.take();
                if (msg.equals(ELVES_IN_TROUBLE_MSG)) {
                    elvesInTroubleSig.release(ELVES_IN_TROUBLE_THRESHOLD);
                    System.out.println("Santa is helping out the elves...");
                    Thread.sleep(2000);
                    System.out.println("Santa helped out the elves in trouble...");
                    elvesTroubleResolvedSig.release(ELVES_IN_TROUBLE_THRESHOLD);
                } else if (msg.equals(REINDEER_ARRIVED_MSG)) {
                    for (int i = 0; i < 9; i++) {
                        reindeerStartHitchSig.release();
                        Thread.sleep(1000);
                        reindeerEndHitchSig.release();
                    }

                    System.out.println("The sleigh is prepared! Merry christmas!");
                    return;
                }
            }
        }));

        // elves
        for (int i = 0; i < 10; i++) {
            final int elfID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 90) {
                        System.out.printf("Elf %d is in trouble...\n", elfID);
                        blockedElves.getAndIncrement();
                        elvesInTroubleSig.acquire();
                        System.out.printf("Elf %d is getting help...\n", elfID);
                        elvesTroubleResolvedSig.acquire();
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        // elves
        for (int i = 0; i < 9; i++) {
            final int reindeerID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 90) {
                        System.out.printf("Reindeer %d has arrived...\n", reindeerID);
                        arrivedReindeer.getAndIncrement();

                        reindeerStartHitchSig.acquire();
                        System.out.printf("Reindeer %d is getting hitched...\n", reindeerID);
                        reindeerEndHitchSig.acquire();
                        System.out.printf("Reindeer %d has been hitched!\n", reindeerID);
                        return;
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        // mediator
        exec.submit(() -> handleInterruptedException(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("thread was interrupted");
                }

                if (blockedElves.get() >= ELVES_IN_TROUBLE_THRESHOLD) {
                    wakeUpSantaMessages.put(ELVES_IN_TROUBLE_MSG);
                    blockedElves.addAndGet(-3);
                }

                if (arrivedReindeer.get() >= 9) {
                    wakeUpSantaMessages.put(REINDEER_ARRIVED_MSG);
                }
            }
        }));

        Thread.sleep(50000);

        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }
}
