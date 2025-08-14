package org.atomspace.values;

/**
 * Base interface for Values in AtomSpace.
 * 
 * Values represent mutable, streaming data that can be attached to Atoms.
 * Unlike Atoms which are immutable and globally unique, Values are lightweight,
 * fast-changing data containers that can represent:
 * - Truth values and probabilities
 * - Streaming sensor data
 * - Neural network weights/activations
 * - Any rapidly changing information
 * 
 * This is a key architectural distinction in AtomSpace that enables
 * efficient separation of stable graph structure (Atoms) from dynamic data (Values).
 */
public interface Value {
    
    /**
     * Get the type of this Value
     */
    ValueType getType();
    
    /**
     * Get a string representation of this Value
     */
    String toString();
    
    /**
     * Check if this Value equals another Value
     */
    boolean equals(Object other);
    
    /**
     * Get hash code for this Value
     */
    int hashCode();
    
    /**
     * Clone this Value (deep copy)
     */
    Value clone();
}