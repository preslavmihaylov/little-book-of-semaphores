package com.pmihaylov.ch05;

import java.util.concurrent.*;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _06_h2o {
    public static void main(String[] args) throws InterruptedException {
        Semaphore hStartBondSig = new Semaphore(0);
        Semaphore oStartBondSig = new Semaphore(0);
        CyclicBarrier bondCompleteBarrier = new CyclicBarrier(4);
        ExecutorService exec = Executors.newCachedThreadPool();

        // h threads
        for (int i = 0; i < 10; i++) {
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 90) {
                        System.out.println("An H cell is waiting to be bonded...");
                        hStartBondSig.acquire();
                        System.out.println("An H cell is bonding...");
                        bondCompleteBarrier.await();
                    }

                    Thread.sleep(1000);
                }
            }));
        }


        // o threads
        for (int i = 0; i < 5; i++) {
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 90) {
                        System.out.println("An O cell is waiting to be bonded...");
                        oStartBondSig.acquire();
                        System.out.println("An O cell is bonding...");
                        bondCompleteBarrier.await();
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        // manager thread
        exec.submit(() -> handleInterruptedException(() -> {
            while (true) {
                if (oStartBondSig.getQueueLength() >= 1 && hStartBondSig.getQueueLength() >= 2) {
                    hStartBondSig.release(2);
                    oStartBondSig.release();

                    System.out.println("Manager thread is waiting for bond to complete...");
                    bondCompleteBarrier.await();
                    System.out.println("H2O bonding is complete!");
                }

                Thread.sleep(500);
            }
        }));

        Thread.sleep(20000);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }
}
