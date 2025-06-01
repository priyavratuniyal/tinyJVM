package com.tinyjvm.interpreter;

import java.util.Stack;
// Assuming JFrame class is defined in this package or will be.
// For example: public class JFrame { /* ... */ }

/**
 * Represents the Java Virtual Machine (JVM) stack, which stores frames.
 * Each frame represents a method invocation for a particular thread.
 */
public class JVMStack {
    private Stack<com.tinyjvm.interpreter.JFrame> frameStack;
    private int maxSize; // Not strictly enforced by java.util.Stack, but for conceptual alignment

    /**
     * Constructs a new JVMStack with a conceptual maximum size.
     *
     * @param stackSize The conceptual maximum size of the stack.
     */
    public JVMStack(int stackSize) {
        this.frameStack = new Stack<>(); // Initialize the stack
        this.maxSize = stackSize; // Store for conceptual use, actual java.util.Stack grows dynamically
    }

    /**
     * Pushes a frame onto the JVM stack.
     *
     * @param frame The frame to push.
     */
    public void push(com.tinyjvm.interpreter.JFrame frame) {
        // In a real JVM, you might check against maxSize here and throw StackOverflowError
        this.frameStack.push(frame);
    }

    /**
     * Pops a frame from the JVM stack.
     *
     * @return The frame popped from the stack.
     * @throws java.util.EmptyStackException if the stack is empty.
     */
    public com.tinyjvm.interpreter.JFrame pop() {
        return this.frameStack.pop();
    }

    /**
     * Peeks at the top frame of the JVM stack without removing it.
     *
     * @return The frame at the top of the stack.
     * @throws java.util.EmptyStackException if the stack is empty.
     */
    public com.tinyjvm.interpreter.JFrame peek() {
        return this.frameStack.peek();
    }

    /**
     * Checks if the JVM stack is empty.
     *
     * @return True if the stack is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.frameStack.isEmpty();
    }

    /**
     * Returns the current number of frames on the stack.
     *
     * @return The number of frames.
     */
    public int size() {
        return this.frameStack.size();
    }
}
