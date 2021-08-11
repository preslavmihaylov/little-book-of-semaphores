package com.pmihaylov.lib;

import java.util.concurrent.Semaphore;

import static com.pmihaylov.common.Utils.ignoreInterruptedException;

public class Mutex {
    private final Semaphore sem = new Semaphore(1);

    public void lock() {
        ignoreInterruptedException(sem::acquire);
    }

    public void unlock() {
        ignoreInterruptedException(sem::release);
    }
}
