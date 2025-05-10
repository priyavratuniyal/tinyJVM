package com.tinyjvm;

public class ConstantPoolEntry {
    public final int tag;
    public final Object value;

    public ConstantPoolEntry(int tag, Object value) {
        this.tag = tag;
        this.value = value;
    }

    @Override
    public String toString() {
        return "CP#" + tag + ": " + value;
    }
}