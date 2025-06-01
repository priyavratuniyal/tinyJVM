package com.tinyjvm.interpreter;

import java.util.Stack;

public class JStack {
    private Stack<JFrame> frameStack = new Stack<>();

    public void push(JFrame frame) {
        this.frameStack.push(frame);
    }

    public JFrame pop() {
        return this.frameStack.pop();
    }

    public JFrame current() {
        return this.frameStack.peek();
    }

    public boolean isEmpty() {
        return this.frameStack.isEmpty();
    }
}
