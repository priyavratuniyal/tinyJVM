package com.tinyjvm.memory;

import com.tinyjvm.threads.Monitor;

/**
 * Represents an object instance in the TinyJVM's heap.
 * Each object has associated data and a monitor for synchronization.
 */
public class JVMObject {
    // In a more detailed JVM, 'data' would represent the object's fields.
    // For this example, it can be a simple placeholder or the actual Java object being represented.
    private final Object data;
    private final Monitor monitor; // Each object has an intrinsic lock (monitor)

    /**
     * Constructs a new JVMObject.
     *
     * @param data The actual data or representation of the object's fields.
     */
    public JVMObject(Object data) {
        this.data = data;
        this.monitor = new Monitor(); // Create a new monitor for each object
        System.out.println("JVMObject: Created. Data: " + (data != null ? data.toString() : "null") + ", Monitor: " + monitor.hashCode());
    }

    /**
     * Gets the monitor associated with this object.
     *
     * @return The monitor for this object.
     */
    public Monitor getMonitor() {
        return monitor;
    }

    /**
     * Gets the data associated with this object.
     * In a real JVM, this would provide access to the object's fields.
     *
     * @return The object's data.
     */
    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "JVMObject{" +
               "data=" + (data != null ? data.toString() : "null") +
               ", monitor=" + monitor.hashCode() +
               '}';
    }
}
