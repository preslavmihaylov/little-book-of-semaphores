package com.pmihaylov.ch03;

import java.util.concurrent.Semaphore;
import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _01_rendezvous {
    public static void main(String[] args) throws InterruptedException {
        Semaphore sem1 = new Semaphore(0);
        Semaphore sem2 = new Semaphore(0);
        new Thread(() -> handleInterruptedException(() -> {
            System.out.println("t1: rendezvous");
            sem1.release();
            sem2.acquire();

            System.out.println("t1: statement");
        })).start();
        new Thread(() -> handleInterruptedException(() -> {
            System.out.println("t2: rendezvous");
            sem2.release();
            sem1.acquire();

            System.out.println("t2: statement");
        })).start();

        Thread.sleep(500);
    }

}
