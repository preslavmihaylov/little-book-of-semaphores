package com.pmihaylov.lib;

import java.util.concurrent.Semaphore;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class Mutex {
    private final Semaphore sem = new Semaphore(1);

    public void lock() {
        handleInterruptedException(sem::acquire);
    }

    public void unlock() {
        handleInterruptedException(sem::release);
    }
}
