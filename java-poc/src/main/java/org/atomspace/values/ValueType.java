package org.atomspace.values;

/**
 * Types of Values that can be stored in the AtomSpace.
 */
public enum ValueType {
    TRUTH_VALUE("TruthValue"),
    FLOAT_VALUE("FloatValue"), 
    STRING_VALUE("StringValue"),
    LINK_VALUE("LinkValue"),
    STREAM_VALUE("StreamValue"),
    BOOL_VALUE("BoolValue"),
    VECTOR_VALUE("VectorValue");
    
    private final String name;
    
    ValueType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}