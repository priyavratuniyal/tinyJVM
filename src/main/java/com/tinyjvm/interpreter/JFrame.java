package com.tinyjvm.interpreter;

import java.util.Stack;

/**
 * Represents a frame in the Java Virtual Machine (JVM) stack.
 * Each frame corresponds to a method invocation and holds its local variables,
 * operand stack, and a reference to the runtime constant pool of the class of the current method.
 */
public class JFrame {

    /**
     * Program counter, indicating the next instruction to be executed.
     */
    public int pc;

    /**
     * Bytecode of the method associated with this frame.
     */
    public byte[] code;

    /**
     * Local variables array.
     */
    private int[] locals;

    /**
     * Operand stack for performing calculations.
     */
    private Stack<Integer> operandStack;

    /**
     * Constructs a new JFrame.
     *
     * @param maxLocals Maximum number of local variables.
     * @param maxStack  Maximum size of the operand stack.
     * @param code      Bytecode of the method.
     */
    public JFrame(int maxLocals, int maxStack, byte[] code) {
        this.pc = 0;
        this.code = code;
        this.locals = new int[maxLocals];
        this.operandStack = new Stack<>();
        // maxStack is not directly used by java.util.Stack,
        // but it's good practice to have it for conceptual clarity
        // or potential future use (e.g. bounded stack).
    }

    /**
     * Gets the value of a local variable at the given index.
     *
     * @param index Index of the local variable.
     * @return Value of the local variable.
     */
    public int getLocal(int index) {
        return locals[index];
    }

    /**
     * Sets the value of a local variable at the given index.
     *
     * @param index Index of the local variable.
     * @param value Value to set.
     */
    public void setLocal(int index, int value) {
        locals[index] = value;
    }

    /**
     * Pushes a value onto the operand stack.
     *
     * @param value Value to push.
     */
    public void push(int value) {
        operandStack.push(value);
    }

    /**
     * Pops a value from the operand stack.
     *
     * @return Value popped from the stack.
     */
    public int pop() {
        return operandStack.pop();
    }
}
