package com.tinyjvm.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * Simulates the JVM's heap memory where objects are stored.
 */
public class JVMHeap {
    private Map<Integer, Object> storage = new HashMap<>();
    private int nextAddress = 1; // Start addresses from 1 for simplicity

    /**
     * Allocates an object on the heap.
     *
     * @param obj The object to allocate.
     * @return The address (reference) of the allocated object.
     */
    public int allocate(Object obj) {
        int address = nextAddress++;
        storage.put(address, obj);
        return address;
    }

    /**
     * Retrieves an object from the heap using its address.
     *
     * @param address The address (reference) of the object.
     * @return The object stored at the given address, or null if the address is invalid.
     */
    public Object get(int address) {
        return storage.get(address);
    }
}
