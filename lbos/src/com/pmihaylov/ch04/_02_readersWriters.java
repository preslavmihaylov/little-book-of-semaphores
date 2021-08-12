package com.pmihaylov.ch04;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _02_readersWriters {
    static int cnt = 0;

    public static void main(String[] args) throws InterruptedException {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        ExecutorService exec = Executors.newCachedThreadPool();

        for (int i = 0; i < 5; i++) {
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    System.out.println("[R] waiting for read lock...");
                    lock.readLock().lock();
                    try {
                        System.out.printf("[R] reading value %d\n", cnt);
                    } finally {
                        lock.readLock().unlock();
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        exec.submit(() -> handleInterruptedException(() -> {
            while (true) {
                lock.writeLock().lock();
                try {
                    System.out.printf("[W] writing value %d\n", ++cnt);
                    Thread.sleep(2000);
                } finally {
                    lock.writeLock().unlock();
                }

                Thread.sleep(2000);
            }
        }));

        Thread.sleep(10000);
        exec.shutdownNow();
        exec.awaitTermination(2, TimeUnit.SECONDS);
    }
}
