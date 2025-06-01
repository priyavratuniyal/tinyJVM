package com.tinyjvm.interpreter;

/**
 * Interprets JVM bytecode. It uses a JVMStack to manage method frames
 * and a JVMHeap for object storage (though heap usage is minimal in this version).
 * The interpreter executes instructions sequentially from the bytecode of the current method frame.
 */
public class BytecodeInterpreter {

    private JVMStack jvmStack;
    private JVMHeap jvmHeap; // Not actively used yet, but part of the structure

    /**
     * Constructs a new BytecodeInterpreter.
     *
     * @param stack The JVM stack to be used for method invocations.
     * @param heap  The JVM heap to be used for object allocations.
     */
    public BytecodeInterpreter(JVMStack stack, JVMHeap heap) {
        this.jvmStack = stack;
        this.jvmHeap = heap;
    }

    /**
     * Runs the bytecode execution starting with an initial frame.
     * This method simulates the JVM execution loop.
     * For testing purposes, if the initial frame returns a value and the stack becomes empty,
     * this method will return that value.
     *
     * @param initialFrame The first frame to start execution from, typically for the main method.
     * @return The integer result from the execution if the initial method returns an int and
     *         completes execution, otherwise 0 or an undefined value if the main method is void.
     */
    public int run(JFrame initialFrame) {
        jvmStack.push(initialFrame);
        Integer result = null;

        while (!jvmStack.isEmpty()) {
            JFrame currentFrame = jvmStack.peek();
            byte[] code = currentFrame.code;
            int opcode = Byte.toUnsignedInt(code[currentFrame.pc++]);

            switch (opcode) {
                case 0x10: // bipush
                    InstructionSet.bipush(currentFrame);
                    break;
                case 0x60: // iadd
                    InstructionSet.iadd(currentFrame);
                    break;
                case 0xac: // ireturn
                    int returnValue = InstructionSet.ireturn(currentFrame);
                    jvmStack.pop(); // Pop current frame
                    if (jvmStack.isEmpty()) {
                        result = returnValue; // Capture result from the last frame
                    } else {
                        // If there's a calling frame, push the return value onto its operand stack
                        jvmStack.peek().push(returnValue);
                    }
                    break;
                case 0xb1: // return (void return)
                    InstructionSet.vreturn(currentFrame);
                    jvmStack.pop(); // Pop current frame
                    if (jvmStack.isEmpty()) {
                        // Main method was void and finished.
                        // Result will remain null, and 0 will be returned as per method contract.
                    }
                    break;
                // iload instructions
                case 0x1a: // iload_0
                    InstructionSet.iload(currentFrame, 0);
                    break;
                case 0x1b: // iload_1
                    InstructionSet.iload(currentFrame, 1);
                    break;
                case 0x1c: // iload_2
                    InstructionSet.iload(currentFrame, 2);
                    break;
                case 0x1d: // iload_3
                    InstructionSet.iload(currentFrame, 3);
                    break;
                // istore instructions
                case 0x3b: // istore_0
                    InstructionSet.istore(currentFrame, 0);
                    break;
                case 0x3c: // istore_1
                    InstructionSet.istore(currentFrame, 1);
                    break;
                case 0x3d: // istore_2
                    InstructionSet.istore(currentFrame, 2);
                    break;
                case 0x3e: // istore_3
                    InstructionSet.istore(currentFrame, 3);
                    break;
                // Placeholder for more generic iload (opcode 0x15) and istore (opcode 0x36)
                // which take an index from the bytecode stream
                case 0x15: // iload
                    {
                        int index = Byte.toUnsignedInt(code[currentFrame.pc++]);
                        InstructionSet.iload(currentFrame, index);
                    }
                    break;
                case 0x36: // istore
                    {
                        int index = Byte.toUnsignedInt(code[currentFrame.pc++]);
                        InstructionSet.istore(currentFrame, index);
                    }
                    break;
                default:
                    // It's often better to let the program crash on unknown opcode
                    // than to continue in an undefined state.
                    throw new UnsupportedOperationException("Opcode not implemented: 0x" + Integer.toHexString(opcode) + " at pc=" + (currentFrame.pc -1));
            }
        }
        return result != null ? result : 0; // Return captured result or 0 if no int result
    }
}
