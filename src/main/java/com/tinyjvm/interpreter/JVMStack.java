package com.tinyjvm.interpreter;

import java.util.Stack;

/**
 * Represents the Java Virtual Machine (JVM) stack, which stores frames.
 * Each frame represents a method invocation.
 */
public class JVMStack {
    private Stack<JFrame> frameStack = new Stack<>();

    /**
     * Pushes a frame onto the JVM stack.
     *
     * @param frame The frame to push.
     */
    public void push(JFrame frame) {
        this.frameStack.push(frame);
    }

    /**
     * Pops a frame from the JVM stack.
     *
     * @return The frame popped from the stack.
     */
    public JFrame pop() {
        return this.frameStack.pop();
    }

    /**
     * Peeks at the top frame of the JVM stack without removing it.
     *
     * @return The frame at the top of the stack.
     */
    public JFrame peek() {
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
}
