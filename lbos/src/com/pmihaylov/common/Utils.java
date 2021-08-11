package com.pmihaylov.common;

public class Utils {
    public static void ignoreInterruptedException(InterruptableRunnable r) {
        try {
            r.run();
        } catch (InterruptedException e) {
            System.out.println("thread was interrupted");
        }
    }

    @FunctionalInterface
    public interface InterruptableRunnable {
        void run() throws InterruptedException;
    }
}
