package com.tinyjvm.threads;

/**
 * Utility class for thread-related operations, such as yielding.
 */
public class ThreadUtils {

    /**
     * Causes the currently executing {@link JVMThread} to pause execution and allow other threads to run.
     * This is a cooperative yield.
     * The current thread's state is set to {@link JVMThread.ThreadState#RUNNABLE},
     * and the {@link Scheduler} is notified to reschedule.
     */
    public static void yield() {
        JVMThread currentThread = Scheduler.getCurrentThread();
        if (currentThread != null) {
            System.out.println("ThreadUtils: Thread " + currentThread.getThreadId() + " is yielding. Setting state to RUNNABLE.");
            currentThread.setState(JVMThread.ThreadState.RUNNABLE);
            Scheduler.reschedule(); // Notify the scheduler to pick another thread
        } else {
            System.err.println("ThreadUtils.yield(): No current thread is executing, cannot yield.");
            // This case should ideally not happen in a well-managed threaded environment
            // when yield is called from within a running thread's code.
        }
    }
}
