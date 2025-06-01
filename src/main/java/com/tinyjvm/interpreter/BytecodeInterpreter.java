package com.tinyjvm.interpreter;

import com.tinyjvm.memory.JVMObject;
import com.tinyjvm.threads.JVMThread;
import com.tinyjvm.threads.Scheduler; // For YIELD

/**
 * Interprets JVM bytecode for a given thread.
 * An instance of BytecodeInterpreter is typically associated with a JVMThread.
 */
public class BytecodeInterpreter {

    // These could be passed per instruction execution, or stored if interpreter is per-thread
    private final JVMHeap sharedHeap; // Shared heap instance

    public static final byte YIELD_OPCODE = (byte) 0xFF; // Custom opcode for cooperative yield

    /**
     * Constructs a new BytecodeInterpreter.
     * In this model, the interpreter itself is stateless regarding the current thread's stack.
     * The stack is managed by the JVMThread and passed implicitly via the current JFrame.
     *
     * @param heap  The shared JVM heap to be used for object allocations and lookups.
     */
    public BytecodeInterpreter(JVMHeap heap) {
        this.sharedHeap = heap;
        // jvmStack is now owned by JVMThread
    }

    /**
     * Executes the current bytecode instruction for the given thread.
     * This method is called by JVMThread.executeNextInstruction().
     *
     * @param thread The currently executing thread.
     * @return {@code true} if execution should continue for this thread (within its quantum),
     *         {@code false} if the thread yielded, returned from its run method, or encountered an error.
     */
    public boolean executeCurrentInstruction(JVMThread thread) {
        JVMStack jvmStack = thread.getStack();
        if (jvmStack.isEmpty()) {
            System.err.println("Interpreter: Thread " + thread.getThreadId() + " has an empty stack. Cannot execute instruction.");
            thread.setState(JVMThread.ThreadState.TERMINATED);
            return false; // Cannot continue
        }

        JFrame currentFrame = jvmStack.peek();
        if (currentFrame == null) { // Should not happen if stack is not empty, but defensive check
             System.err.println("Interpreter: Thread " + thread.getThreadId() + " has null currentFrame. Terminating.");
             thread.setState(JVMThread.ThreadState.TERMINATED);
             return false;
        }

        if (currentFrame.pc >= currentFrame.code.length) {
            System.err.println("Interpreter: Thread " + thread.getThreadId() + " PC beyond code length. Method likely missing return. Popping frame.");
            jvmStack.pop();
            if (jvmStack.isEmpty()) {
                thread.setState(JVMThread.ThreadState.TERMINATED);
                System.out.println("Interpreter: Thread " + thread.getThreadId() + " terminated after auto-pop from PC overrun.");
                return false; // Thread terminated
            }
            return true; // Continue with next instruction in new top frame (if any) or terminate if stack empty
        }

        byte opcode = currentFrame.code[currentFrame.pc++];
        thread.setProgramCounter(currentFrame.pc); // Keep thread's PC synced with frame's PC

        System.out.println("Thread " + thread.getThreadId() + " (PC:" + (currentFrame.pc-1) + ") Executing opcode: 0x" + String.format("%02X", opcode));

        switch (Byte.toUnsignedInt(opcode)) {
            case 0x10: // bipush
                InstructionSet.bipush(currentFrame);
                break;
            case 0x60: // iadd
                InstructionSet.iadd(currentFrame);
                break;
            case 0xac: // ireturn
                int returnValue = InstructionSet.ireturn(currentFrame);
                jvmStack.pop();
                if (jvmStack.isEmpty()) {
                    System.out.println("Thread " + thread.getThreadId() + " returned from initial method with value: " + returnValue + ". Terminating.");
                    thread.setState(JVMThread.ThreadState.TERMINATED);
                    return false; // Thread's main method returned, so thread terminates
                } else {
                    jvmStack.peek().push(returnValue);
                }
                break;
            case 0xb1: // return (void return)
                InstructionSet.vreturn(currentFrame);
                jvmStack.pop();
                if (jvmStack.isEmpty()) {
                    System.out.println("Thread " + thread.getThreadId() + " returned void from initial method. Terminating.");
                    thread.setState(JVMThread.ThreadState.TERMINATED);
                    return false; // Thread's main method returned, so thread terminates
                }
                break;
            // iload instructions
            case 0x1a: InstructionSet.iload(currentFrame, 0); break;
            case 0x1b: InstructionSet.iload(currentFrame, 1); break;
            case 0x1c: InstructionSet.iload(currentFrame, 2); break;
            case 0x1d: InstructionSet.iload(currentFrame, 3); break;
            // istore instructions
            case 0x3b: InstructionSet.istore(currentFrame, 0); break;
            case 0x3c: InstructionSet.istore(currentFrame, 1); break;
            case 0x3d: InstructionSet.istore(currentFrame, 2); break;
            case 0x3e: InstructionSet.istore(currentFrame, 3); break;
            case 0x15: // iload
                InstructionSet.iload(currentFrame, Byte.toUnsignedInt(currentFrame.code[currentFrame.pc++]));
                thread.setProgramCounter(currentFrame.pc);
                break;
            case 0x36: // istore
                InstructionSet.istore(currentFrame, Byte.toUnsignedInt(currentFrame.code[currentFrame.pc++]));
                thread.setProgramCounter(currentFrame.pc);
                break;

            // Synchronization Opcodes
            case 0xC2: // monitorenter
                Object objRefEnter = currentFrame.pop();
                if (objRefEnter instanceof JVMObject) {
                    JVMObject jvmObjEnter = (JVMObject) objRefEnter;
                    System.out.println("Thread " + thread.getThreadId() + " attempting MONITORENTER on " + jvmObjEnter);
                    jvmObjEnter.getMonitor().enter(thread);
                    // If enter() caused the thread to block, its state will be BLOCKED.
                    // The executeNextInstruction in JVMThread should check this state.
                    if (thread.getState() == JVMThread.ThreadState.BLOCKED) {
                        // PC should not advance if blocked before instruction completes conceptually
                        currentFrame.pc--;
                        thread.setProgramCounter(currentFrame.pc);
                        return false; // Yield execution because blocked
                    }
                } else {
                    throw new IllegalStateException("MONITORENTER expects a JVMObject on stack, got: " + (objRefEnter != null ? objRefEnter.getClass().getName() : "null"));
                }
                break;

            case 0xC3: // monitorexit
                Object objRefExit = currentFrame.pop();
                if (objRefExit instanceof JVMObject) {
                    JVMObject jvmObjExit = (JVMObject) objRefExit;
                    System.out.println("Thread " + thread.getThreadId() + " attempting MONITOREXIT on " + jvmObjExit);
                    jvmObjExit.getMonitor().exit(thread); // Can throw IllegalMonitorStateException
                } else {
                    throw new IllegalStateException("MONITOREXIT expects a JVMObject on stack, got: " + (objRefExit != null ? objRefExit.getClass().getName() : "null"));
                }
                break;

            case Byte.toUnsignedInt(YIELD_OPCODE): // Custom YIELD (0xFF)
                System.out.println("Thread " + thread.getThreadId() + " executing YIELD_OPCODE.");
                thread.setState(JVMThread.ThreadState.RUNNABLE);
                // Scheduler.reschedule(); // Implicitly handled by returning false
                return false; // Signal to scheduler to yield this thread's quantum

            default:
                throw new UnsupportedOperationException("Opcode not implemented: 0x" + String.format("%02X", opcode) + " at pc=" + (currentFrame.pc -1) + " in thread " + thread.getThreadId());
        }
        return true; // Continue execution for this thread
    }
}
