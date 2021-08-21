package com.pmihaylov.ch04;

import com.pmihaylov.lib.Mutex;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.pmihaylov.ch04._04_diningPhilosophersTanenbaum.PhilosopherState.*;

public class _04_diningPhilosophersTanenbaum {
    enum PhilosopherState {
        THINKING, HUNGRY, EATING
    }

    static final int PHILOSOPHERS_CNT = 5;
    static final int SIMULATION_TIME_MS = 20000;
    static List<Mutex> forks = new ArrayList<>();
    static List<PhilosopherState> states = new ArrayList<>();
    static List<Integer> mealsCnt = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        init();
        ExecutorService exec = Executors.newFixedThreadPool(5);
        for (int i = 0; i < PHILOSOPHERS_CNT; i++) {
            final int philosopherIdx = i;
            exec.submit(() -> {
                PhilosopherState prevState = THINKING;
                while (true) {
                    Random rng = ThreadLocalRandom.current();
                    PhilosopherState currState = getNextState(philosopherIdx);
                    if (currState.equals(THINKING)) {
                        System.out.printf("Philosopher %d is thinking...\n", philosopherIdx);
                        Thread.sleep(2000 + (rng.nextInt() % 1000));
                    } else if (currState.equals(HUNGRY)) {
                        if (prevState.equals(HUNGRY)) {
                            Thread.sleep(1000 + (rng.nextInt() % 500));
                        } else {
                            System.out.printf("Philosopher %d is hungry & waiting for forks...\n", philosopherIdx);
                        }
                    } else if (currState.equals(EATING)) {
                        System.out.printf("Philosopher %d is eating...\n", philosopherIdx);
                        Thread.sleep(2000 + (rng.nextInt() % 1000));
                    }

                    prevState = currState;
                }
            });
        }

        Thread.sleep(SIMULATION_TIME_MS);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\nMeals breakdown:");
        for (int i = 0; i < PHILOSOPHERS_CNT; i++) {
            System.out.printf("Philosopher %d:\t%d\n", i, mealsCnt.get(i));
        }
    }

    public static void init() {
        for (int i = 0; i < PHILOSOPHERS_CNT; i++) {
            forks.add(new Mutex());
            states.add(THINKING);
            mealsCnt.add(0);
        }
    }

    public static synchronized PhilosopherState getNextState(int philosopherIdx) {
        PhilosopherState state = states.get(philosopherIdx);
        if (state.equals(THINKING)) {
            states.set(philosopherIdx, HUNGRY);
            return HUNGRY;
        } else if (state.equals(HUNGRY)) {
            if (states.get(leftIndex(philosopherIdx)) != EATING &&
                    states.get(rightIndex(philosopherIdx)) != EATING) {

                forks.get(leftFork(philosopherIdx)).lock();
                forks.get(rightFork(philosopherIdx)).lock();
                states.set(philosopherIdx, EATING);
                mealsCnt.set(philosopherIdx, mealsCnt.get(philosopherIdx) + 1);
                return EATING;
            } else {
                return HUNGRY;
            }
        } else {
            forks.get(leftFork(philosopherIdx)).unlock();
            forks.get(rightFork(philosopherIdx)).unlock();
            states.set(philosopherIdx, THINKING);
            return THINKING;
        }
    }

    public static int leftIndex(int idx) {
        return (idx-1) < 0 ? (PHILOSOPHERS_CNT-1) : idx-1;
    }

    public static int rightIndex(int idx) {
        return (idx+1)%PHILOSOPHERS_CNT;
    }

    public static int leftFork(int idx) {
        return (idx+1)%PHILOSOPHERS_CNT;
    }

    public static int rightFork(int idx) {
        return idx;
    }
}
