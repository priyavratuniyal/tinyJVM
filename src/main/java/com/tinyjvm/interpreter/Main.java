package com.tinyjvm.interpreter;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize JVM Components
        JVMStack jvmStack = new JVMStack();
        JVMHeap jvmHeap = new JVMHeap(); // Not used by this specific bytecode, but good practice

        // 2. Instantiate Interpreter
        BytecodeInterpreter interpreter = new BytecodeInterpreter(jvmStack, jvmHeap);

        // 3. Update Example Bytecode and Frame Setup
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
                (byte) 0xac        // ireturn
        };

        // maxLocals = 3 (valA, valB, sum)
        // maxStack = 2 (after two iloads, before iadd)
        JFrame initialFrame = new JFrame(3, 2, code);

        // Set initial values for arguments valA (local var 0) and valB (local var 1)
        initialFrame.setLocal(0, 5);  // valA = 5
        initialFrame.setLocal(1, 10); // valB = 10

        // 4. Run Interpreter
        try {
            // The run method now returns the result from the initial frame if it's an int return
            int result = interpreter.run(initialFrame);
            System.out.println("Execution completed.");
            // The result of the 'add' method (5 + 10 = 15)
            System.out.println("Returned: " + result);

            // For verification, we can also check the state of the initialFrame's locals
            // if needed, but the interpreter has finished, so the frame is popped.
            // To see local var 2 (sum), we would need to inspect it *before* ireturn if possible,
            // or rely on the returned value. The bytecode loads local var 2 to stack before returning.
            // The value 15 should be returned.

        } catch (Exception e) {
            System.err.println("Error during interpretation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
