package com.pmihaylov.ch03;

import com.pmihaylov.lib.Barrier;

import static com.pmihaylov.common.Utils.ignoreInterruptedException;

public class _04_barrier {
    public static void main(String[] args) {
        Barrier b = new Barrier(5);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> ignoreInterruptedException(() -> {
                System.out.printf("Thread %s arrived at rendezvous\n", Thread.currentThread().getName());
                b.hold();
                System.out.printf("Thread %s arrived at critical point\n", Thread.currentThread().getName());
            })).start();
        }
    }
}
