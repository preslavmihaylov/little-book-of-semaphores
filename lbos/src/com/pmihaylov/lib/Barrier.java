package com.pmihaylov.lib;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Barrier {
    private final Semaphore sem;
    private final AtomicInteger cnt;
    private final int n;

    public Barrier(int n) {
        sem = new Semaphore(0);
        cnt = new AtomicInteger(n);
        this.n = n;
    }

    public void hold() throws InterruptedException {
        cnt.getAndDecrement();
        if (cnt.get() <= 0) {
            sem.release(n);
        } else {
            sem.acquire();
        }
    }
}
