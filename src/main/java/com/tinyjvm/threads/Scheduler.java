package com.tinyjvm.threads;

import com.tinyjvm.utils.Logger;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Manages the scheduling and execution of JVMThreads in a cooperative manner.
 * It uses a round-robin algorithm to give each thread a chance to run.
 */
public class Scheduler {
    private static Scheduler instance;
    private final Queue<JVMThread> runQueue = new LinkedList<>();
    private int quantum = 100; // Default number of instructions per thread quantum
    private static JVMThread currentThread = null; // Track the currently executing thread

    /**
     * Private constructor for Singleton pattern.
     */
    private Scheduler() {}

    /**
     * Gets the singleton instance of the Scheduler.
     *
     * @return The singleton Scheduler instance.
     */
    public static synchronized Scheduler getInstance() {
        if (instance == null) {
            instance = new Scheduler();
        }
        return instance;
    }

    /**
     * Registers a thread with the scheduler, adding it to the run queue.
     * The thread should be in the RUNNABLE state.
     *
     * @param thread The JVMThread to register.
     */
    public void registerThread(JVMThread thread) {
        if (thread != null && thread.getState() == JVMThread.ThreadState.RUNNABLE) {
            runQueue.add(thread);
            Logger.info("Scheduler: Registered thread " + thread.getThreadId() + ". Run queue size: " + runQueue.size());
        } else {
            Logger.error("Scheduler: Could not register thread. It might be null or not in RUNNABLE state: " + thread);
        }
    }

    /**
     * Starts the scheduling loop.
     * This method will continuously pick threads from the run queue and execute them
     * for a defined quantum until the run queue is empty.
     */
    public void start() {
        Logger.info("Scheduler: Starting execution. Quantum: " + quantum + " instructions.");
        while (!runQueue.isEmpty()) {
            JVMThread threadToRun = runQueue.poll();
            if (threadToRun == null) continue;

            Logger.debug("Scheduler: Picking thread " + threadToRun.getThreadId() + " from run queue. State: " + threadToRun.getState());

            if (threadToRun.getState() == JVMThread.ThreadState.TERMINATED) {
                Logger.debug("Scheduler: Thread " + threadToRun.getThreadId() + " is already TERMINATED. Skipping.");
                continue;
            }

            // If a thread is BLOCKED or WAITING, it should not be in the runQueue unless
            // it was just made RUNNABLE by a monitor or notification.
            // If it's here and still in BLOCKED/WAITING, it's a state issue or it needs to be handled by monitor logic primarily.
            if (threadToRun.getState() == JVMThread.ThreadState.BLOCKED || threadToRun.getState() == JVMThread.ThreadState.WAITING) {
                Logger.debug("Scheduler: Thread " + threadToRun.getThreadId() + " is " + threadToRun.getState() + ". It should become RUNNABLE before being processed. Re-queuing for now.");
                runQueue.add(threadToRun); // Re-queue with the hope its state will be updated by other mechanisms.
                // Potentially add a small delay or use a separate queue for such threads to avoid busy spinning.
                continue;
            }

            if (threadToRun.getState() != JVMThread.ThreadState.RUNNABLE) {
                 Logger.debug("Scheduler: Thread " + threadToRun.getThreadId() + " is in state "+ threadToRun.getState() +" instead of RUNNABLE. Re-queuing.");
                 runQueue.add(threadToRun); // Re-queue, expecting state to become RUNNABLE.
                 continue;
            }

            threadToRun.setState(JVMThread.ThreadState.RUNNING);
            currentThread = threadToRun;
            Logger.debug("Scheduler: Executing thread " + threadToRun.getThreadId() + " (State: " + threadToRun.getState() + ")");

            executeThreadQuantum(threadToRun);
            currentThread = null; // Clear current thread after execution attempt

            // Re-queue if still runnable or was running (meaning it yielded or quantum ended)
            if (threadToRun.getState() == JVMThread.ThreadState.RUNNABLE) {
                Logger.debug("Scheduler: Re-queuing thread " + threadToRun.getThreadId() + " (State: " + threadToRun.getState() + ")");
                runQueue.add(threadToRun);
            } else if (threadToRun.getState() == JVMThread.ThreadState.RUNNING) {
                 // If it's still RUNNING, it means its quantum finished abruptly or without a state change by executeNextInstruction.
                 // Set to RUNNABLE before re-queuing.
                threadToRun.setState(JVMThread.ThreadState.RUNNABLE);
                Logger.debug("Scheduler: Thread " + threadToRun.getThreadId() + " quantum ended, set to RUNNABLE and re-queued.");
                runQueue.add(threadToRun);
            } else if (threadToRun.getState() == JVMThread.ThreadState.TERMINATED) {
                Logger.info("Scheduler: Thread " + threadToRun.getThreadId() + " terminated.");
            } else {
                // If BLOCKED or WAITING, it should be re-registered by the Monitor/Object.wait logic
                // when it becomes RUNNABLE. It should not be re-queued automatically here if in these states.
                Logger.debug("Scheduler: Thread " + threadToRun.getThreadId() + " finished quantum in state " + threadToRun.getState() + ". Not re-queuing automatically.");
            }
        }
        Logger.info("Scheduler: All dispatchable threads have completed. Run queue is empty.");
    }

    /**
     * Executes a given thread for a defined quantum of instructions.
     *
     * @param thread The JVMThread to execute.
     */
    private void executeThreadQuantum(JVMThread thread) {
        int executedInstructions = 0;
        // The thread must be in RUNNING state to execute instructions.
        while (executedInstructions < quantum && thread.getState() == JVMThread.ThreadState.RUNNING) {
            if (!thread.executeNextInstruction()) {
                // executeNextInstruction returns false if thread terminates, yields, or an error occurs.
                // The state (TERMINATED or RUNNABLE for yield) should be set within executeNextInstruction or by YIELD bytecode.
                Logger.debug("Scheduler: Thread " + thread.getThreadId() + " signaled to stop its current execution slice (e.g. yielded, terminated, or error). State: " + thread.getState());
                break;
            }
            executedInstructions++;
        }
        Logger.debug("Scheduler: Thread " + thread.getThreadId() + " finished quantum part. Executed: " + executedInstructions + " instructions. Final state in quantum: " + thread.getState());

        // If thread is still RUNNING after the loop (quantum exhausted without self-yield/termination),
        // set it to RUNNABLE so it can be re-queued by the main loop.
        if (thread.getState() == JVMThread.ThreadState.RUNNING) {
            thread.setState(JVMThread.ThreadState.RUNNABLE);
        }
    }

    /**
     * Gets the currently executing thread.
     *
     * @return The currently running JVMThread, or null if no thread is running.
     */
    public static JVMThread getCurrentThread() {
        return currentThread;
    }

    /**
     * Called when a thread intends to yield control.
     * The yielding thread should ideally set its own state to RUNNABLE before calling this.
     * In our cooperative model, the main scheduler loop will handle re-queuing.
     * This method mainly serves as a hook or for logging.
     */
    public static void reschedule() {
        JVMThread self = getCurrentThread(); // Use 'self' which is defined
        if (self != null) {
            Logger.debug("Scheduler: Thread " + self.getThreadId() + " is yielding. Current state: " + self.getState());
            // Ensure its state is RUNNABLE if it's yielding.
            // This should typically be done by the YIELD bytecode or the method calling yield.
            if (self.getState() == JVMThread.ThreadState.RUNNING) {
                 self.setState(JVMThread.ThreadState.RUNNABLE);
            }
            // The main loop in start() will pick it up if it's RUNNABLE and re-queue it.
            // The executeThreadQuantum loop also breaks when executeNextInstruction returns false (on yield).
        } else {
            Logger.error("Scheduler: reschedule() called but no current thread is set!");
        }
    }

    /**
     * Sets the instruction quantum for thread execution.
     * @param newQuantum The number of instructions per timeslice.
     */
    public void setQuantum(int newQuantum) {
        this.quantum = newQuantum > 0 ? newQuantum : 100;
    }
}
