package com.tinyjvm.interpreter;

/**
 * Defines the set of JVM instructions and their execution logic.
 * Each method typically takes the current JFrame as a parameter to manipulate
 * its local variables, operand stack, or program counter.
 */
public class InstructionSet {

    /**
     * Pushes a byte onto the operand stack as an integer.
     * The byte is read from the bytecode stream immediately following the bipush opcode.
     * The pc of the frame is advanced past the byte.
     *
     * @param frame The current execution frame.
     */
    public static void bipush(JFrame frame) {
        // In Java, bytes are signed. frame.code[frame.pc++] will be sign-extended to int.
        // This is the correct behavior for bipush.
        int value = frame.code[frame.pc++];
        frame.push(value);
    }

    /**
     * Adds the top two integers on the operand stack and pushes the result back.
     *
     * @param frame The current execution frame.
     */
    public static void iadd(JFrame frame) {
        int b = frame.pop();
        int a = frame.pop();
        frame.push(a + b);
    }

    /**
     * Returns an integer from the current method.
     * The integer value is taken from the top of the operand stack of the current frame.
     * This instruction signifies the completion of a method that returns an int.
     * The actual handling of transferring the return value to the caller frame
     * and popping the current frame is managed by the interpreter loop.
     *
     * @param frame The current execution frame.
     * @return The integer value to be returned from the method.
     */
    public static int ireturn(JFrame frame) {
        // The value returned by this method is the actual integer result
        // that was on top of the operand stack. The interpreter
        // will take this value and push it onto the calling frame's stack if needed.
        return frame.pop();
    }

    /**
     * Returns from a method that has a void return type.
     * This instruction signifies the completion of a void method.
     * The interpreter loop will handle popping the current frame.
     *
     * @param frame The current execution frame. (unused for 'return', but kept for consistency)
     */
    public static void vreturn(JFrame frame) {
        // 'return' (for void) instruction in JVM. Opcode 0xb1.
        // No value is returned. The interpreter will just pop the current frame.
        // The parameter 'frame' is not strictly needed here but included for consistency
        // in method signature style with other instructions.
    }

    /**
     * Loads an integer from a local variable and pushes it onto the operand stack.
     *
     * @param frame The current execution frame.
     * @param index The index of the local variable.
     */
    public static void iload(JFrame frame, int index) {
        int value = frame.getLocal(index);
        frame.push(value);
    }

    /**
     * Stores an integer from the top of the operand stack into a local variable.
     *
     * @param frame The current execution frame.
     * @param index The index of the local variable.
     */
    public static void istore(JFrame frame, int index) {
        int value = frame.pop();
        frame.setLocal(index, value);
    }
}
