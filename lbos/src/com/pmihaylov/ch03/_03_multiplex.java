package com.pmihaylov.ch03;

import java.util.concurrent.Semaphore;

import static com.pmihaylov.common.Utils.ignoreInterruptedException;

public class _03_multiplex {
    public static void main(String[] args) throws InterruptedException {
        Semaphore sem = new Semaphore(5);
        for (int i = 0; i < 20; i++) {
            new Thread(() -> ignoreInterruptedException(() -> {
                sem.acquire();
                try {
                    System.out.printf("%s is executing statement & sleeping for 2s\n", Thread.currentThread().getName());
                    Thread.sleep(2000);
                } finally {
                    sem.release();
                }
            })).start();
        }

        Thread.sleep(10000);
    }
}
