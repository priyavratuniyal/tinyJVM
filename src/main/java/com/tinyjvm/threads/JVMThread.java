package com.tinyjvm.threads;

import com.tinyjvm.utils.Logger;
import com.tinyjvm.interpreter.BytecodeInterpreter;
import com.tinyjvm.interpreter.JVMHeap; // Required for interpreter instantiation
import com.tinyjvm.interpreter.JVMStack;
import com.tinyjvm.interpreter.JFrame; // For when target.run() might create a frame

public class JVMThread {
    // ... (idCounter, threadId, state, stack, programCounter, target, ThreadState enum are the same)
    private static int idCounter = 1;
    private final int threadId;
    private volatile ThreadState state;
    private final JVMStack stack;
    private int programCounter;
    private final Runnable target;
    private final BytecodeInterpreter interpreter; // Interpreter instance for this thread

    // Assuming JVMHeap is a singleton for now, for interpreter instantiation.
    // This should be refined later if heap is managed differently.
    private static JVMHeap sharedHeapInstance;
    public static void setSharedHeapInstance(JVMHeap heap) { sharedHeapInstance = heap; }

    public enum ThreadState { NEW, RUNNABLE, RUNNING, BLOCKED, WAITING, TERMINATED }

    public JVMThread(Runnable target, int stackSize) {
        this.threadId = idCounter++;
        this.state = ThreadState.NEW;
        this.target = target;
        this.stack = new JVMStack(stackSize);
        this.programCounter = 0;
        if (sharedHeapInstance == null) {
            // This is a temporary measure. Heap should be properly initialized and passed.
            // throw new IllegalStateException("Shared JVMHeap instance not set for JVMThread constructor.");
            // For now, allow it to be null if tests don't immediately use opcodes needing heap.
            Logger.error("Warning: JVMThread created but sharedHeapInstance is null. Interpreter might fail for heap operations.");
        }
        this.interpreter = new BytecodeInterpreter(sharedHeapInstance);
    }

    // start(), getters, setters, toString() mostly remain the same...
    public void start() {
        if (state != ThreadState.NEW) {
            throw new IllegalThreadStateException("Thread has already been started.");
        }
        state = ThreadState.RUNNABLE;
        Logger.info("Thread " + threadId + " state set to RUNNABLE. Target: " + (target != null ? target.getClass().getName() : "null"));
        Scheduler.getInstance().registerThread(this); // Now we can uncomment this
    }

    public boolean executeNextInstruction() {
        if (state != ThreadState.RUNNING) {
            // If the scheduler tried to run a non-RUNNING thread (e.g. it was set to RUNNABLE after yield)
            // this can happen. Only execute if truly RUNNING.
            // Or, the scheduler should ensure it only calls on RUNNING threads.
            // For now, let's be defensive.
            Logger.debug("Thread " + threadId + " executeNextInstruction called when not RUNNING. State: " + state);
            return state == ThreadState.RUNNABLE; // If runnable, it's okay, scheduler will handle. If other, it's an issue.
        }

        // Initial execution of target.run() if stack is empty
        if (stack.isEmpty()) {
            if (target != null) {
                Logger.debug("Thread " + threadId + " [State:" + state + "]: Executing target.run() for the first time.");
                try {
                    target.run(); // This might push frames and set up for bytecode execution
                } catch (Exception e) {
                    Logger.error("Thread " + threadId + " threw an exception in target.run(): " + e.getMessage());
                    state = ThreadState.TERMINATED;
                    return false;
                }

                if (stack.isEmpty()) {
                    // If target.run() was self-contained and didn't push frames for bytecode.
                    state = ThreadState.TERMINATED;
                    Logger.info("Thread " + threadId + " [State:" + state + "]: Terminated (target.run() completed and no new frames pushed).");
                    return false;
                }
                // If frames were pushed, execution will proceed to interpreter below.
            } else {
                state = ThreadState.TERMINATED;
                Logger.info("Thread " + threadId + " [State:" + state + "]: Terminated (no target and stack empty).");
                return false;
            }
        }

        // Proceed with bytecode interpretation if frames exist
        boolean shouldContinue = interpreter.executeCurrentInstruction(this);

        // If MONITORENTER caused a block, executeCurrentInstruction returns false & state is BLOCKED.
        // If YIELD, executeCurrentInstruction returns false & state is RUNNABLE.
        // If method return led to empty stack, executeCurrentInstruction returns false & state is TERMINATED.
        // No further state change needed here based on `shouldContinue` directly,
        // as interpreter method is expected to manage state for yield/terminate/block.
        return shouldContinue;
    }

    // getters/setters (threadId, state, stack, pc, target) as before
    public int getThreadId() { return threadId; }
    public ThreadState getState() { return state; }
    public void setState(ThreadState state) { this.state = state; }
    public JVMStack getStack() { return stack; }
    public int getProgramCounter() { return programCounter; }
    public void setProgramCounter(int pc) { this.programCounter = pc; }
    public Runnable getTarget() { return target; }
    @Override public String toString() { return "JVMThread[ID=" + threadId + ", State=" + state + "]"; }
}
