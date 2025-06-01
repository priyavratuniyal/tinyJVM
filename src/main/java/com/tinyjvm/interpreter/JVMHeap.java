package com.tinyjvm.interpreter;

import com.tinyjvm.memory.JVMObject;
import com.tinyjvm.threads.JVMThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates the JVM's heap memory where objects are stored.
 * This version tracks allocations per thread and stores JVMObject instances.
 */
public class JVMHeap {
    // Primary storage for objects, mapping an address to the JVMObject.
    // This allows objects to be referenced by a simple integer ID (address).
    private final Map<Integer, JVMObject> objectStore = new HashMap<>();
    private final AtomicInteger nextAddress = new AtomicInteger(1); // Thread-safe address generation

    // Tracks allocations per thread, as suggested by the article.
    private final Map<JVMThread, List<JVMObject>> allocationsByThread = new HashMap<>();

    /**
     * Allocates a new JVMObject on the heap, associated with a specific thread.
     *
     * @param thread The thread for which the object is being allocated.
     * @param data   The raw data to be wrapped by the JVMObject. This could be field data,
     *               or a representation of a Java object's content.
     * @return The allocated JVMObject.
     */
    public JVMObject allocate(JVMThread thread, Object data) {
        JVMObject jvmObject = new JVMObject(data); // JVMObject itself creates a Monitor
        int address = nextAddress.getAndIncrement();
        objectStore.put(address, jvmObject);

        // Track allocation by thread
        synchronized (allocationsByThread) { // Synchronize access to the allocationsByThread map
            allocationsByThread.computeIfAbsent(thread, k -> new ArrayList<>()).add(jvmObject);
        }

        System.out.println("JVMHeap: Allocated object " + jvmObject + " at address " + address + " for thread " + (thread != null ? thread.getThreadId() : "null") );
        // The 'address' could be stored in the JVMObject if needed for identity or GC purposes,
        // but for now, the reference to JVMObject itself is key.
        return jvmObject;
    }

    /**
     * Retrieves a JVMObject from the heap using its address (ID).
     *
     * @param address The address (ID) of the object.
     * @return The JVMObject stored at the given address, or null if the address is invalid.
     */
    public JVMObject getObjectByAddress(int address) {
        return objectStore.get(address);
    }

    /**
     * Gets all objects allocated by a specific thread.
     *
     * @param thread The thread whose allocated objects are to be retrieved.
     * @return A list of JVMObjects allocated by the thread, or an empty list if none.
     */
    public List<JVMObject> getObjectsAllocatedByThread(JVMThread thread) {
        synchronized (allocationsByThread) { // Synchronize access
            return new ArrayList<>(allocationsByThread.getOrDefault(thread, new ArrayList<>()));
        }
    }

    /**
     * Returns the total number of objects currently in the heap's object store.
     * @return Total number of objects.
     */
    public int getTotalObjectsInHeap() {
        return objectStore.size();
    }

    /**
     * Returns the number of threads that have allocated objects on this heap.
     * @return Number of threads with allocations.
     */
    public int getThreadAllocationCount() {
        synchronized (allocationsByThread) {
            return allocationsByThread.size();
        }
    }
}
