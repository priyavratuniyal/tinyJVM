package com.tinyjvm.examples;

import com.tinyjvm.interpreter.JVMHeap;
import com.tinyjvm.memory.JVMObject;
import com.tinyjvm.threads.JVMThread;
import com.tinyjvm.threads.Scheduler;
import com.tinyjvm.threads.ThreadUtils;

public class SyncTest {
    static int counter = 0;
    // The lock object needs to be a JVMObject to use our custom Monitor
    static JVMObject lockObject;

    public static void main(String[] args) {
        JVMHeap heap = new JVMHeap();
        JVMThread.setSharedHeapInstance(heap);

        // Allocate the lock object on our custom heap
        lockObject = heap.allocate(null, "GlobalLockForCounter");

        System.out.println("SyncTest: Starting. Initial counter: " + counter);

        final int numThreads = 10;
        final int incrementsPerThread = 1000;

        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            new JVMThread(() -> {
                // Each thread will attempt to increment the counter a number of times
                for (int j = 0; j < incrementsPerThread; j++) {
                    JVMThread currentJvmThread = Scheduler.getCurrentThread();
                    if (currentJvmThread == null) {
                        // This can happen if the thread starts and getCurrentThread is called
                        // before the scheduler has officially set it via currentThread = threadToRun;
                        // For this test, we'll just retry or skip if it's null in a tight loop.
                        // A better way would be to pass the JVMThread instance into the Runnable if possible,
                        // or ensure Scheduler.getCurrentThread() is robust for early calls.
                        // For now, let's assume it becomes non-null quickly.
                        int retries = 0;
                        while( (currentJvmThread = Scheduler.getCurrentThread()) == null && retries < 10) {
                            ThreadUtils.yield(); // yield to allow scheduler to set current thread
                            retries++;
                        }
                        if (currentJvmThread == null) {
                             System.err.println("Thread " + threadNum + ": Could not get current JVMThread instance for monitor enter after retries.");
                             continue; // Skip this increment if thread context is not found
                        }
                    }

                    lockObject.getMonitor().enter(currentJvmThread);
                    try {
                        int temp = counter;
                        // Yield to force contention and test if synchronization is working
                        ThreadUtils.yield();
                        counter = temp + 1;
                    } finally {
                        lockObject.getMonitor().exit(currentJvmThread);
                    }
                }
                System.out.println("Thread " + threadNum + " finished its increments.");
            }, 2048).start(); // 2048 stack size
        }

        System.out.println("SyncTest: All threads created. Starting scheduler.");
        Scheduler.getInstance().start(); // Start the scheduler

        System.out.println("SyncTest: Scheduler finished.");
        System.out.println("Final counter value: " + counter);
        if (counter == numThreads * incrementsPerThread) {
            System.out.println("SyncTest: SUCCESS! Counter is correct.");
        } else {
            System.err.println("SyncTest: FAILED! Counter is " + counter + ", expected " + (numThreads * incrementsPerThread));
        }
    }
}
