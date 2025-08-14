package org.atomspace.values;

import java.util.Arrays;

/**
 * FloatValue stores a vector of floating-point numbers.
 * 
 * This is commonly used for:
 * - Neural network weights and activations
 * - Feature vectors  
 * - Statistical data
 * - Sensor readings over time
 */
public class FloatValue implements Value {
    private final double[] values;
    
    public FloatValue(double... values) {
        this.values = values.clone(); // Defensive copy
    }
    
    public double[] getValues() {
        return values.clone(); // Return defensive copy
    }
    
    public double getValue(int index) {
        if (index < 0 || index >= values.length) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + values.length);
        }
        return values[index];
    }
    
    public int size() {
        return values.length;
    }
    
    /**
     * Create a new FloatValue by applying a function to each element
     */
    public FloatValue map(java.util.function.DoubleUnaryOperator function) {
        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            newValues[i] = function.applyAsDouble(values[i]);
        }
        return new FloatValue(newValues);
    }
    
    /**
     * Add this FloatValue to another elementwise
     */
    public FloatValue add(FloatValue other) {
        if (values.length != other.values.length) {
            throw new IllegalArgumentException("Cannot add FloatValues of different lengths: " 
                                             + values.length + " vs " + other.values.length);
        }
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i] + other.values[i];
        }
        return new FloatValue(result);
    }
    
    /**
     * Compute dot product with another FloatValue
     */
    public double dotProduct(FloatValue other) {
        if (values.length != other.values.length) {
            throw new IllegalArgumentException("Cannot compute dot product of FloatValues of different lengths: " 
                                             + values.length + " vs " + other.values.length);
        }
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i] * other.values[i];
        }
        return sum;
    }
    
    @Override
    public ValueType getType() {
        return ValueType.FLOAT_VALUE;
    }
    
    @Override
    public Value clone() {
        return new FloatValue(values);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FloatValue)) return false;
        FloatValue other = (FloatValue) obj;
        return Arrays.equals(values, other.values);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
    
    @Override
    public String toString() {
        if (values.length <= 5) {
            return "FloatValue" + Arrays.toString(values);
        } else {
            return String.format("FloatValue[%d elements: %.3f, %.3f, %.3f, ...]", 
                               values.length, values[0], values[1], values[2]);
        }
    }
}