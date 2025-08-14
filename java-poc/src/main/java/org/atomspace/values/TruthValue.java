package org.atomspace.values;

/**
 * TruthValue represents probabilistic truth in AtomSpace.
 * 
 * This is a core concept from Probabilistic Logic Networks (PLN).
 * Each truth value contains:
 * - strength: The probability (0.0 to 1.0)  
 * - confidence: How confident we are in this probability (0.0 to 1.0)
 * 
 * This allows the system to reason under uncertainty and accumulate
 * evidence over time.
 */
public class TruthValue implements Value {
    private final double strength;
    private final double confidence;
    
    public static final TruthValue TRUE = new TruthValue(1.0, 1.0);
    public static final TruthValue FALSE = new TruthValue(0.0, 1.0);
    public static final TruthValue UNKNOWN = new TruthValue(0.5, 0.0);
    
    public TruthValue(double strength, double confidence) {
        if (strength < 0.0 || strength > 1.0) {
            throw new IllegalArgumentException("Strength must be between 0.0 and 1.0, got: " + strength);
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0, got: " + confidence);
        }
        this.strength = strength;
        this.confidence = confidence;
    }
    
    public double getStrength() {
        return strength;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    /**
     * Calculate the "count" - a measure of evidence strength
     * Higher confidence with moderate strength suggests more evidence
     */
    public double getCount() {
        return confidence / (1.0 - confidence + 1e-10); // Avoid division by zero
    }
    
    /**
     * Combine this truth value with another using PLN revision rule
     */
    public TruthValue revise(TruthValue other) {
        double count1 = this.getCount();
        double count2 = other.getCount();
        double totalCount = count1 + count2;
        
        if (totalCount < 1e-10) {
            return UNKNOWN;
        }
        
        double newStrength = (this.strength * count1 + other.strength * count2) / totalCount;
        double newConfidence = totalCount / (totalCount + 1.0);
        
        return new TruthValue(newStrength, newConfidence);
    }
    
    @Override
    public ValueType getType() {
        return ValueType.TRUTH_VALUE;
    }
    
    @Override
    public Value clone() {
        return new TruthValue(strength, confidence);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TruthValue)) return false;
        TruthValue other = (TruthValue) obj;
        return Double.compare(strength, other.strength) == 0 &&
               Double.compare(confidence, other.confidence) == 0;
    }
    
    @Override
    public int hashCode() {
        return Double.hashCode(strength) * 31 + Double.hashCode(confidence);
    }
    
    @Override
    public String toString() {
        return String.format("TruthValue(%.3f, %.3f)", strength, confidence);
    }
}