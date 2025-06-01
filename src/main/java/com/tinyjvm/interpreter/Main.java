package com.tinyjvm.interpreter;

import com.tinyjvm.threads.JVMThread;
import com.tinyjvm.threads.Scheduler;

/**
 * Main class to demonstrate running a simple bytecode sequence using the
 * new threaded execution model (albeit single-threaded in this example).
 */
public class Main {
    public static void main(String[] args) {
        // 1. Initialize JVM Components
        System.out.println("Interpreter Main: Initializing Heap...");
        JVMHeap jvmHeap = new JVMHeap();
        JVMThread.setSharedHeapInstance(jvmHeap); // Set the shared heap for all threads

        // 2. Define the task (bytecode execution) for our main thread
        Runnable mainTask = () -> {
            JVMThread currentThread = Scheduler.getCurrentThread();
            if (currentThread == null) {
                // This can happen if the runnable executes before the scheduler fully assigns currentThread.
                // This is a bit of a race in this direct testing setup.
                // For a more robust setup, JVMThread instance might be passed to Runnable.
                System.err.println("Interpreter Main: Critical - Could not get current thread for initial frame setup.");
                // Attempt to get it from the thread that is running this runnable, if possible
                // This is complex; the design implies Scheduler.getCurrentThread() is the way.
                // Let's assume for this example, the scheduler sets currentThread before this lambda runs.
                // A brief yield might allow the scheduler to set it.
                try { Thread.sleep(1); } catch (InterruptedException e) {}
                currentThread = Scheduler.getCurrentThread();
                if (currentThread == null) {
                     System.err.println("Interpreter Main: Still no current thread. Aborting task.");
                     return;
                }
            }

            JVMStack jvmStack = currentThread.getStack();

            System.out.println("Interpreter Main: Setting up initial frame for thread " + currentThread.getThreadId());

            // Bytecode for:
            // public static int add(int valA, int valB) { // valA = local[0], valB = local[1]
            //    int sum = valA + valB; // sum = local[2]
            //    return sum;
            // }
            byte[] code = new byte[]{
                    (byte) 0x1a,       // iload_0 (load valA from local var 0)
                    (byte) 0x1b,       // iload_1 (load valB from local var 1)
                    (byte) 0x60,       // iadd
                    (byte) 0x3d,       // istore_2 (store sum in local var 2)
                    (byte) 0x1c,       // iload_2 (load sum from local var 2 for return)
                    (byte) 0xac        // ireturn (integer return)
            };

            JFrame initialFrame = new JFrame(3, 2, code); // maxLocals=3, maxStack=2
            initialFrame.setLocal(0, 5);  // valA = 5
            initialFrame.setLocal(1, 10); // valB = 10

            jvmStack.push(initialFrame);
            System.out.println("Interpreter Main: Initial frame pushed to stack of thread " + currentThread.getThreadId());
            // The BytecodeInterpreter within JVMThread will take over from here.
        };

        // 3. Create and Start the Main JVMThread
        System.out.println("Interpreter Main: Creating main JVMThread.");
        JVMThread mainThread = new JVMThread(mainTask, 256); // Runnable task, stack size 256
        mainThread.start(); // Registers with scheduler

        // 4. Start the Scheduler
        // The scheduler will pick up the mainThread and execute its target.run(), then its bytecode.
        System.out.println("Interpreter Main: Starting Scheduler...");
        Scheduler.getInstance().start();

        System.out.println("Interpreter Main: Execution finished.");
        // Note: Retrieving a 'result' like before is more complex now as execution is asynchronous
        // and happens within a thread. The result would typically be observed via side effects
        // (e.g., System.out.println from bytecode) or by joining/waiting for the thread if we implement that.
        // The old BytecodeInterpreter.run() returned the int; here, the thread terminates.
        // The ireturn will print a message from within BytecodeInterpreter if it's the last frame.
    }
}
