package com.pmihaylov.ch05;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _07_riverCrossing {
    private static boolean isBoatRowed = false;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        Semaphore passengerArrivedSig = new Semaphore(0);

        AtomicInteger queuedHackers = new AtomicInteger(0);
        AtomicInteger queuedSerfers = new AtomicInteger(0);
        Semaphore hackersQueue = new Semaphore(0);
        Semaphore serfersQueue = new Semaphore(0);
        CyclicBarrier boatBoardedBarrier = new CyclicBarrier(4);
        CyclicBarrier boatUnboardedBarrier = new CyclicBarrier(5);

        // hackers
        for (int i = 0; i < 10; i++) {
            final int hackerID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 90) {
                        System.out.printf("Hacker #%d arrived at boat...\n", hackerID);
                        queuedHackers.getAndIncrement();
                        passengerArrivedSig.release();
                        hackersQueue.acquire();

                        System.out.printf("Hacker #%d boarded the boat...\n", hackerID);
                        boatBoardedBarrier.await();
                        synchronized (_07_riverCrossing.class) {
                            if (!isBoatRowed) {
                                System.out.printf("Hacker #%d is rowing the boat...\n", hackerID);
                                Thread.sleep(1000);
                                isBoatRowed = true;
                            }
                        }

                        boatUnboardedBarrier.await();
                        System.out.printf("Hacker #%d unboarded the boat...\n", hackerID);
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        // serfers
        for (int i = 0; i < 10; i++) {
            final int serfID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 90) {
                        System.out.printf("Serfer #%d arrived at boat...\n", serfID);
                        queuedSerfers.getAndIncrement();
                        passengerArrivedSig.release();
                        serfersQueue.acquire();

                        System.out.printf("Serfer #%d boarded the boat...\n", serfID);
                        boatBoardedBarrier.await();
                        synchronized (_07_riverCrossing.class) {
                            if (!isBoatRowed) {
                                System.out.printf("Serfer #%d is rowing the boat...\n", serfID);
                                Thread.sleep(1000);
                                isBoatRowed = true;
                            }
                        }

                        boatUnboardedBarrier.await();
                        System.out.printf("Serfer #%d unboarded the boat...\n", serfID);
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        // manager
        exec.submit(() -> handleInterruptedException(() -> {
            while (true) {
                passengerArrivedSig.acquire();
                if (queuedHackers.get() >= 4) {
                    synchronized (_07_riverCrossing.class) {
                        isBoatRowed = false;
                    }

                    System.out.println("4 hackers are let through with the boat...");
                    queuedHackers.addAndGet(-4);
                    hackersQueue.release(4);

                    boatUnboardedBarrier.await();
                } else if (queuedSerfers.get() >= 4) {
                    synchronized (_07_riverCrossing.class) {
                        isBoatRowed = false;
                    }

                    System.out.println("4 serfers are let through with the boat...");
                    queuedSerfers.addAndGet(-4);
                    serfersQueue.release(4);

                    boatUnboardedBarrier.await();
                } else if (queuedHackers.get() >= 2 && queuedSerfers.get() >= 2) {
                    synchronized (_07_riverCrossing.class) {
                        isBoatRowed = false;
                    }

                    System.out.println("2 hackers and 2 serfers are let through with the boat...");
                    queuedSerfers.addAndGet(-2);
                    serfersQueue.release(2);
                    queuedHackers.addAndGet(-2);
                    hackersQueue.release(2);

                    boatUnboardedBarrier.await();
                }
            }
        }));

        Thread.sleep(20000);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }
}
