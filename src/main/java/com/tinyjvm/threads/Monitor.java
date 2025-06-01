package com.tinyjvm.threads;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Implements a monitor for object synchronization, similar to Java's intrinsic locks.
 * This monitor supports reentrant locking and a queue for threads waiting to acquire the lock.
 * It uses a dedicated lock object for internal synchronization of its state.
 */
public class Monitor {
    private volatile JVMThread owner = null;        // The thread that currently owns the monitor, null if unlocked
    private volatile int entryCount = 0;          // Reentrancy count: how many times the owner has entered
    private final Queue<JVMThread> entryQueue = new LinkedList<>(); // Threads waiting to acquire this monitor
    private final Object internalLock = new Object(); // Dedicated lock for synchronizing access to monitor's state

    /**
     * Acquires the monitor. If the monitor is already owned by another thread,
     * the calling thread will be blocked and placed in the entry queue until the monitor is free.
     * If the calling thread already owns the monitor, the entry count is incremented (reentrancy).
     *
     * @param thread The JVMThread attempting to acquire the monitor.
     * @throws NullPointerException if the provided thread is null.
     */
    public void enter(JVMThread thread) {
        if (thread == null) {
            throw new NullPointerException("Thread attempting to enter monitor cannot be null");
        }

        // Fast path for reentrancy: check if current thread is already the owner.
        // This initial check is outside the synchronized block for performance.
        if (owner == thread) {
            synchronized (internalLock) {
                // Double-check inside synchronized block to ensure atomicity if 'owner' changed.
                if (owner == thread) {
                    entryCount++;
                    System.out.println("Monitor: Thread " + thread.getThreadId() + " re-entered. Owner: " + (owner != null ? owner.getThreadId() : "none") + ". New Entry Count: " + entryCount);
                    return;
                }
            }
        }

        boolean acquired = false;
        while (!acquired) {
            synchronized (internalLock) {
                if (owner == null) {
                    // Monitor is free, acquire it.
                    owner = thread;
                    entryCount = 1;
                    acquired = true;
                    System.out.println("Monitor: Thread " + thread.getThreadId() + " acquired lock. Owner: " + owner.getThreadId() + ". Entry Count: " + entryCount);
                    // Remove from entry queue if it was added in a previous iteration (e.g. lost race, then parked)
                    entryQueue.remove(thread);

                    // Ensure the thread is in a runnable state for the scheduler.
                    // If it was BLOCKED by this monitor, it needs to become RUNNABLE and be known to scheduler.
                    if (thread.getState() == JVMThread.ThreadState.BLOCKED) {
                        thread.setState(JVMThread.ThreadState.RUNNABLE);
                        Scheduler.getInstance().registerThread(thread);
                    }
                } else {
                    // Monitor is owned by another thread, or we lost a race.
                    // Add to entry queue if not already present.
                    if (!entryQueue.contains(thread)) {
                        System.out.println("Monitor: Thread " + thread.getThreadId() + " found lock held by " + (owner != null ? owner.getThreadId() : "unknown") + ". Adding to entry queue ("+ entryQueue.size() +" waiting).");
                        entryQueue.add(thread);
                    }
                }
            } // End of synchronized block

            if (!acquired) {
                // If lock was not acquired, park the thread cooperatively.
                // Set state to BLOCKED before parking.
                thread.setState(JVMThread.ThreadState.BLOCKED);
                System.out.println("Monitor: Thread " + thread.getThreadId() + " is BLOCKED by monitor, parking cooperatively.");
                parkThreadCooperatively(thread); // This call will yield until the thread's state is no longer BLOCKED.
                System.out.println("Monitor: Thread " + thread.getThreadId() + " unparked or state changed (now "+thread.getState()+"), re-contending for lock.");
            }
        }
    }

    /**
     * Releases the monitor. If the calling thread is not the owner, an
     * IllegalMonitorStateException is thrown. If the entry count becomes zero
     * as a result of this exit, the monitor becomes unowned. If there are threads
     * waiting in the entry queue, one is chosen, made RUNNABLE, and registered with the scheduler.
     *
     * @param thread The JVMThread attempting to release the monitor.
     * @throws IllegalMonitorStateException if the calling thread is not the owner.
     * @throws NullPointerException if the provided thread is null.
     */
    public void exit(JVMThread thread) {
        if (thread == null) {
            throw new NullPointerException("Thread attempting to exit monitor cannot be null");
        }

        synchronized (internalLock) {
            if (owner != thread) {
                throw new IllegalMonitorStateException("Thread " + thread.getThreadId() + " is not the owner of this monitor. Current owner: " + (owner != null ? owner.getThreadId() : "none"));
            }

            entryCount--;
            System.out.println("Monitor: Thread " + thread.getThreadId() + " exited monitor. Owner: " + (owner != null ? owner.getThreadId() : "none") + ". New Entry Count: " + entryCount);

            if (entryCount == 0) {
                owner = null;
                System.out.println("Monitor: Lock fully released by thread " + thread.getThreadId());
                if (!entryQueue.isEmpty()) {
                    JVMThread nextThreadToWake = entryQueue.poll(); // Retrieve and remove the head of the queue.
                    if (nextThreadToWake != null) {
                        System.out.println("Monitor: Notifying thread " + nextThreadToWake.getThreadId() + " from entry queue to become RUNNABLE.");
                        nextThreadToWake.setState(JVMThread.ThreadState.RUNNABLE);
                        // Crucially, the scheduler must be informed that this thread is now runnable.
                        Scheduler.getInstance().registerThread(nextThreadToWake);
                    }
                }
            }
        }
    }

    /**
     * Cooperatively parks the calling thread. The thread's state should be set to BLOCKED
     * before calling this method or within the enter method before this is called.
     * This method will repeatedly call the scheduler's reschedule
     * mechanism (yield) as long as the thread remains in the BLOCKED state.
     *
     * @param thread The thread to park (which should be the current thread of execution).
     */
    private void parkThreadCooperatively(JVMThread thread) {
        // This thread is already marked BLOCKED by the enter() method before this call.
        System.out.println("Monitor: parkThreadCooperatively for thread " + thread.getThreadId() + ". State: " + thread.getState() + ". Yielding control.");

        // Loop as long as the thread is marked BLOCKED.
        // An external action (like unparking in exit()) must change its state to RUNNABLE.
        while (thread.getState() == JVMThread.ThreadState.BLOCKED) {
            Scheduler.reschedule(); // Signal to scheduler to run other threads.
                                  // This is our equivalent of Thread.yield() from the article's Monitor.
        }
        // When the loop exits, the thread's state is no longer BLOCKED.
        System.out.println("Monitor: Thread " + thread.getThreadId() + " is no longer BLOCKED in parkThreadCooperatively. Current state: " + thread.getState());
    }

    /**
     * Checks if the given thread is the current owner of this monitor.
     * @param thread The thread to check.
     * @return true if the specified thread owns this monitor, false otherwise.
     */
    public boolean isOwner(JVMThread thread) {
        synchronized(internalLock) {
            return owner == thread;
        }
    }
}
