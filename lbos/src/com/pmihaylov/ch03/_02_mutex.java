package com.pmihaylov.ch03;

import com.pmihaylov.lib.Mutex;

public class _02_mutex {
    public static void main(String[] args) throws InterruptedException {
        SyncCounter cnt = new SyncCounter();
        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                cnt.inc();
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                cnt.inc();
            }
        }).start();

        Thread.sleep(500);
        System.out.println(cnt.get());
    }

    static class SyncCounter {
        private final Mutex mux = new Mutex();
        private int cnt = 0;

        public void inc() {
            mux.lock();
            cnt++;
            mux.unlock();
        }

        public int get() {
            mux.lock();
            try {
                return cnt;
            } finally {
                mux.unlock();
            }
        }
    }
}
