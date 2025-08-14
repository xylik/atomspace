package org.atomspace.values;

/**
 * StringValue stores a string value.
 */
public class StringValue implements Value {
    private final String value;
    
    public StringValue(String value) {
        this.value = value != null ? value : "";
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public ValueType getType() {
        return ValueType.STRING_VALUE;
    }
    
    @Override
    public Value clone() {
        return new StringValue(value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StringValue)) return false;
        StringValue other = (StringValue) obj;
        return value.equals(other.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public String toString() {
        return "StringValue(\"" + value + "\")";
    }
}