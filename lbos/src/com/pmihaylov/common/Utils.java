package com.pmihaylov.common;

import java.util.concurrent.BrokenBarrierException;

public class Utils {
    public static void handleInterruptedException(InterruptableRunnable r) {
        try {
            r.run();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println("thread was interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("Unexpected exception occured: " + e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface InterruptableRunnable {
        void run() throws InterruptedException, BrokenBarrierException;
    }
}
