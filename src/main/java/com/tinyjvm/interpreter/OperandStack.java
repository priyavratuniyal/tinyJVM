package com.tinyjvm.interpreter;

public class OperandStack {
    private int[] stack;
    private int top;

    public OperandStack(int maxSize) {
        this.stack = new int[maxSize];
        this.top = -1;
    }

    public void push(int value) {
        if (top + 1 >= stack.length) {
            throw new StackOverflowError("Operand stack overflow");
        }
        stack[++top] = value;
    }

    public int pop() {
        if (top < 0) {
            throw new IllegalStateException("Operand stack underflow");
        }
        int value = stack[top];
        top--;
        return value;
    }

    public int peek() {
        if (top < 0) {
            throw new IllegalStateException("Operand stack is empty");
        }
        return stack[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public int size() {
        return top + 1;
    }
}
