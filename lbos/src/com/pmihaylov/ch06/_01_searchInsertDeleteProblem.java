package com.pmihaylov.ch06;

import com.pmihaylov.common.Utils.InterruptableRunnable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.pmihaylov.common.Utils.handleInterruptedException;

public class _01_searchInsertDeleteProblem {
    private static volatile List<Integer> list = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        ReadWriteLock deleteLock = new ReentrantReadWriteLock();
        Lock insertLock = new ReentrantLock();

        // readers
        for (int i = 0; i < 10; i++) {
            final int readerID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 80) {
                        System.out.printf("Reader %d is waiting to read the list...\n", readerID);
                        locked(deleteLock.readLock(), () -> {
                            System.out.printf("Reader %d is reading the list...\n", readerID);
                            Thread.sleep(1000);
                            System.out.printf("Reader %d has read: [%s]...\n", readerID, list.stream().map(Object::toString).collect(Collectors.joining(", ")));
                        });
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        // deleters
        for (int i = 0; i < 3; i++) {
            final int deleterID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 80) {
                        System.out.printf("Deleter %d is waiting to delete an element from the list...\n", deleterID);
                        locked(deleteLock.writeLock(), () -> {
                            System.out.printf("Deleter %d is deleting an element from the list...\n", deleterID);
                            Thread.sleep(2000);
                            if (list.size() > 0) {
                                int toDelete = Math.abs(ThreadLocalRandom.current().nextInt()) % list.size();
                                int deleted = list.get(toDelete);
                                list.remove(toDelete);
                                System.out.printf("Deleter %d has deleted element %d...\n", deleterID, deleted);
                            } else {
                                System.out.printf("List is empty. Deleter %d didn't have any elements to delete...\n", deleterID);
                            }
                        });
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        // inserters
        for (int i = 0; i < 6; i++) {
            final int inserterID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 > 80) {
                        System.out.printf("Inserter %d is waiting to insert an element into the list...\n", inserterID);
                        locked(deleteLock.readLock(), () -> {
                            locked(insertLock, () -> {
                                System.out.printf("Inserter %d is inserting an element into the list...\n", inserterID);
                                Thread.sleep(2000);

                                int inserted = ThreadLocalRandom.current().nextInt() % 100;
                                list.add(inserted);
                                System.out.printf("Inserter %d has inserted %d into the list...\n", inserterID, inserted);
                            });
                        });
                    }

                    Thread.sleep(1000);
                }
            }));
        }

        Thread.sleep(30000);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void locked(Lock lock, InterruptableRunnable r) {
        lock.lock();
        try {
            handleInterruptedException(r);
        } finally {
            lock.unlock();
        }
    }
}
