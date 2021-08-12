package com.pmihaylov.common;

public class Utils {
    public static void handleInterruptedException(InterruptableRunnable r) {
        try {
            r.run();
        } catch (InterruptedException e) {
            System.out.println("thread was interrupted");
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    public interface InterruptableRunnable {
        void run() throws InterruptedException;
    }
}
