package com.pmihaylov.ch05;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

import static com.pmihaylov.common.Utils.handleInterruptedException;

// limitations to original problem:
//  * payment can happen in parallel rather than once per barber
public class _03_hilzerBarbershop {
    private static final int SOFA_SEATS_CNT = 2;
    private static final int TOTAL_SPACE = 4;
    private static final int WAITING_ROOM_CNT = TOTAL_SPACE - SOFA_SEATS_CNT;

    private static final int SLEEP_OFFSET = 1000;
    private static final int BARBERS_CNT = 2;

    private static int waitingRoomSeats = WAITING_ROOM_CNT;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();

        Queue<String> messages = new ConcurrentLinkedDeque<>();
        BlockingQueue<Map.Entry<CyclicBarrier, CyclicBarrier>> customerServiceQueue = new LinkedBlockingQueue<>(BARBERS_CNT);

        Semaphore waitingRoomSig = new Semaphore(SOFA_SEATS_CNT, true);
        Semaphore sofaSig = new Semaphore(0, true);
        Semaphore customerWaitingSig = new Semaphore(0);

        // barber
        for (int i = 0; i < BARBERS_CNT; i++) {
            final int barberId = i;
            exec.submit(() -> handleInterruptedException(() -> {
                CyclicBarrier customerServiceStart = new CyclicBarrier(2);
                CyclicBarrier customerServiceEnd = new CyclicBarrier(2);
                while (true) {
                    messages.add(String.format("[B] Barber %d goes to sleep...", barberId));
                    customerWaitingSig.acquire();
                    messages.add(String.format("[B] Barber %d wakes up...", barberId));

                    sofaSig.release();
                    waitingRoomSig.release();

                    customerServiceQueue.put(new AbstractMap.SimpleEntry<>(customerServiceStart, customerServiceEnd));
                    customerServiceStart.await();
                    messages.add(String.format("[B] Barber %d is cutting the customer's hair...", barberId));
                    Thread.sleep(SLEEP_OFFSET + 2000 + (ThreadLocalRandom.current().nextInt() % 1000));

                    messages.add(String.format("[B] Barber %d is processing customer's payment...", barberId));
                    Thread.sleep(SLEEP_OFFSET + 1000 + (ThreadLocalRandom.current().nextInt() % 1000));
                    customerServiceEnd.await();
                }
            }));
        }

        // clients
        for (int i = 0; i < 10; i++) {
            final int customerID = i;
            exec.submit(() -> handleInterruptedException(() -> {
                while (true) {
                    if (ThreadLocalRandom.current().nextInt() % 100 <= 20) {
                        Thread.sleep(SLEEP_OFFSET + 1000 + (ThreadLocalRandom.current().nextInt() % 5000));
                        continue;
                    }

                    synchronized (_03_hilzerBarbershop.class) {
                        if (waitingRoomSeats <= 0) {
                            messages.add(String.format("[C] Client %d leaves as there are no free chairs...", customerID));
                            Thread.sleep(SLEEP_OFFSET + 1000 + (ThreadLocalRandom.current().nextInt() % 5000));
                            continue;
                        }

                        waitingRoomSeats--;
                    }

                    customerWaitingSig.release();
                    messages.add(String.format("[C] Client %d is in the waiting room...", customerID));
                    waitingRoomSig.acquire();

                    synchronized (_03_hilzerBarbershop.class) {
                        waitingRoomSeats++;
                    }

                    messages.add(String.format("[C] Client %d is waiting on the sofa...", customerID));
                    sofaSig.acquire();

                    Map.Entry<CyclicBarrier, CyclicBarrier> customerServiceBarriers = customerServiceQueue.take();
                    customerServiceBarriers.getKey().await();
                    messages.add(String.format("[C] Client %d is getting a haircut...", customerID));
                    customerServiceBarriers.getValue().await();

                    messages.add(String.format("[C] Client %d is serviced successfully & leaves!", customerID));
                    Thread.sleep(SLEEP_OFFSET + 1000 + (ThreadLocalRandom.current().nextInt() % 5000));
                }
            }));
        }

        Thread.sleep(20000);
        exec.shutdownNow();
        exec.awaitTermination(5, TimeUnit.SECONDS);
        while (!messages.isEmpty()) {
            System.out.println(messages.remove());
        }
    }
}
