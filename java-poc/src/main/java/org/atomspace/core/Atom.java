package org.atomspace.core;

import org.atomspace.types.AtomType;
import org.atomspace.values.Value;
import org.atomspace.values.TruthValue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.Objects;

/**
 * Atom is the fundamental building block of AtomSpace.
 * 
 * Key properties that make AtomSpace unique:
 * 
 * 1. IMMUTABLE: Once created, an Atom's structure never changes
 * 2. GLOBALLY UNIQUE: There is only one instance of any given Atom
 * 3. TYPED: Each Atom has a specific type in a rich type hierarchy  
 * 4. INDEXED: Atoms are efficiently indexed for rapid graph traversal
 * 5. DECORATED: Mutable Values can be attached to provide dynamic data
 * 
 * This design enables efficient hypergraph storage and advanced pattern matching
 * while supporting dynamic data through the Atom/Value distinction.
 */
public abstract class Atom {
    
    // Unique ID generator
    private static final AtomicLong nextId = new AtomicLong(1);
    
    // Core immutable properties
    private final long id;
    private final AtomType type;
    private final int hashCode;
    
    // Mutable key-value store for Values (thread-safe)
    private final ConcurrentHashMap<String, Value> values;
    
    // Incoming links - other atoms that reference this one
    private final ConcurrentHashMap<Atom, Boolean> incomingSet;
    
    protected Atom(AtomType type) {
        this.id = nextId.getAndIncrement();
        this.type = type;
        this.values = new ConcurrentHashMap<>();
        this.incomingSet = new ConcurrentHashMap<>();
        this.hashCode = computeHashCode();
        
        // Set default truth value
        setTruthValue(TruthValue.UNKNOWN);
    }
    
    /**
     * Get the unique ID of this Atom
     */
    public final long getId() {
        return id;
    }
    
    /**
     * Get the type of this Atom
     */
    public final AtomType getType() {
        return type;
    }
    
    /**
     * Set a Value on this Atom (mutable)
     */
    public void setValue(String key, Value value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
    }
    
    /**
     * Get a Value from this Atom
     */
    public Value getValue(String key) {
        return values.get(key);
    }
    
    /**
     * Get all Values on this Atom
     */
    public Map<String, Value> getAllValues() {
        return new ConcurrentHashMap<>(values);
    }
    
    /**
     * Remove a Value from this Atom
     */
    public Value removeValue(String key) {
        return values.remove(key);
    }
    
    /**
     * Check if this Atom has a Value for the given key
     */
    public boolean hasValue(String key) {
        return values.containsKey(key);
    }
    
    /**
     * Set the truth value (convenience method)
     */
    public void setTruthValue(TruthValue tv) {
        setValue("truth-value", tv);
    }
    
    /**
     * Get the truth value (convenience method)  
     */
    public TruthValue getTruthValue() {
        Value tv = getValue("truth-value");
        return tv instanceof TruthValue ? (TruthValue) tv : TruthValue.UNKNOWN;
    }
    
    /**
     * Add an incoming link (called automatically by AtomSpace)
     */
    public void addIncomingAtom(Atom atom) {
        incomingSet.put(atom, Boolean.TRUE);
    }
    
    /**
     * Remove an incoming link (called automatically by AtomSpace)
     */
    public void removeIncomingAtom(Atom atom) {
        incomingSet.remove(atom);
    }
    
    /**
     * Get all incoming atoms (atoms that reference this one)
     */
    public java.util.Set<Atom> getIncomingSet() {
        return incomingSet.keySet();
    }
    
    /**
     * Get the number of incoming atoms
     */
    public int getIncomingSetSize() {
        return incomingSet.size();
    }
    
    /**
     * Check if this Atom is referenced by other atoms
     */
    public boolean hasIncomingAtoms() {
        return !incomingSet.isEmpty();
    }
    
    /**
     * Compute hash code based on immutable properties only
     */
    protected abstract int computeHashCode();
    
    /**
     * Get the arity (number of outgoing atoms) - 0 for Nodes, variable for Links
     */
    public abstract int getArity();
    
    /**
     * Get string representation for debugging
     */
    public abstract String toDebugString();
    
    @Override
    public final int hashCode() {
        return hashCode;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Atom other = (Atom) obj;
        return hashCode == other.hashCode && contentEquals(other);
    }
    
    /**
     * Check content equality (used by equals() method)
     */
    protected abstract boolean contentEquals(Atom other);
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(type.getName());
        appendContent(sb);
        
        // Add truth value if not default
        TruthValue tv = getTruthValue();
        if (!TruthValue.UNKNOWN.equals(tv)) {
            sb.append(" ").append(tv);
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * Append content-specific string representation
     */
    protected abstract void appendContent(StringBuilder sb);
}