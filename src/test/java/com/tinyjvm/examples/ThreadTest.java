package com.tinyjvm.examples;

import com.tinyjvm.interpreter.JVMHeap;
import com.tinyjvm.memory.JVMObject;
import com.tinyjvm.threads.JVMThread;
import com.tinyjvm.threads.Scheduler;
import com.tinyjvm.threads.ThreadUtils;

public class ThreadTest {
    public static void main(String[] args) {
        // Initialize the shared heap instance for JVMThread to use
        // In a real JVM, heap initialization would be part of startup
        JVMHeap heap = new JVMHeap();
        JVMThread.setSharedHeapInstance(heap); // Set the shared heap

        System.out.println("ThreadTest: Starting test with custom JVMObjects and Monitors.");

        // Create a shared JVMObject to be used as a lock
        // The data "LockObject" is just for identification
        final JVMObject lock = heap.allocate(null, "LockObject");
        // 'null' for thread in allocate if it's a globally available object not tied to a specific thread's direct alloc count
        // or, we can assign it to a conceptual "main" thread if one exists.
        // For this test, passing null is fine as allocate primarily uses it for the allocationsByThread map.

        new JVMThread(() -> {
            System.out.println("Thread A: Attempting to acquire lock...");
            lock.getMonitor().enter(Scheduler.getCurrentThread());
            System.out.println("Thread A: Lock acquired.");
            try {
                for (int i = 0; i < 5; i++) {
                    System.out.println("Thread A: " + i);
                    if (i == 2) {
                        System.out.println("Thread A: Intentionally causing a small delay at i=2");
                        // Simulate some work with a tiny real sleep, not TinyJVM yield
                        try { Thread.sleep(10); } catch (InterruptedException e) {}
                    }
                    ThreadUtils.yield(); // Use our custom yield
                }
            } finally {
                System.out.println("Thread A: Releasing lock.");
                lock.getMonitor().exit(Scheduler.getCurrentThread());
            }
            System.out.println("Thread A: Finished.");
        }, 1024).start(); // 1024 is stack size

        new JVMThread(() -> {
            System.out.println("Thread B: Attempting to acquire lock...");
            lock.getMonitor().enter(Scheduler.getCurrentThread());
            System.out.println("Thread B: Lock acquired.");
            try {
                for (int i = 0; i < 5; i++) {
                    System.out.println("Thread B: " + i);
                    ThreadUtils.yield(); // Use our custom yield
                }
            } finally {
                System.out.println("Thread B: Releasing lock.");
                lock.getMonitor().exit(Scheduler.getCurrentThread());
            }
            System.out.println("Thread B: Finished.");
        }, 1024).start();

        System.out.println("ThreadTest: Starting scheduler...");
        Scheduler.getInstance().start();
        System.out.println("ThreadTest: Scheduler finished.");
    }
}
