package org.opencog.atomspace;

/**
 * Core Java representation of an Atom in the AtomSpace.
 * This provides the foundation for Java AST -> AtomSpace import.
 */
public abstract class Atom {
    private final String type;
    private final String structuralKey;
    
    protected Atom(String type, String structuralKey) {
        this.type = type;
        this.structuralKey = structuralKey;
    }
    
    public String getType() {
        return type;
    }
    
    public String getStructuralKey() {
        return structuralKey;
    }
    
    /**
     * Generate a canonical structural key for this atom.
     * This implements the content-addressed identity system.
     */
    public abstract String computeStructuralKey();
    
    /**
     * Convert this atom to its AtomSpace representation.
     */
    public abstract String toAtomese();
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Atom)) return false;
        Atom atom = (Atom) other;
        return structuralKey.equals(atom.structuralKey);
    }
    
    @Override
    public int hashCode() {
        return structuralKey.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s[%s]", type, structuralKey);
    }
}