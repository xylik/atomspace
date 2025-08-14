package org.atomspace.core;

import org.atomspace.types.AtomType;

import java.util.*;

/**
 * Link represents relationships between atoms (hyperedges).
 * 
 * Links connect other atoms together, forming the hypergraph structure.
 * They have an ordered list of outgoing atoms they connect to. Examples:
 * 
 * - (InheritanceLink (ConceptNode "cat") (ConceptNode "mammal"))
 * - (EvaluationLink (PredicateNode "likes") (ListLink (ConceptNode "John") (ConceptNode "pizza")))
 * 
 * This corresponds to edges in traditional graph databases, but with key differences:
 * 1. Can connect to any number of atoms (hyperedge)
 * 2. Are immutable and globally unique
 * 3. Can themselves be connected to by other links
 * 4. Support complex nested structures
 */
public class Link extends Atom {
    
    private final List<Atom> outgoing;
    
    /**
     * Create a new Link with the given type and outgoing atoms
     */
    public Link(AtomType type, List<Atom> outgoing) {
        super(type);
        if (!type.isLink()) {
            throw new IllegalArgumentException("AtomType " + type + " is not a Link type");
        }
        
        this.outgoing = Collections.unmodifiableList(new ArrayList<>(outgoing));
        
        // Register this link as incoming to all outgoing atoms
        for (Atom atom : this.outgoing) {
            if (atom != null) {
                atom.addIncomingAtom(this);
            }
        }
    }
    
    /**
     * Convenience constructor for variable arguments
     */
    public Link(AtomType type, Atom... outgoing) {
        this(type, Arrays.asList(outgoing));
    }
    
    /**
     * Get the outgoing atoms
     */
    public List<Atom> getOutgoing() {
        return outgoing;
    }
    
    /**
     * Get a specific outgoing atom by index
     */
    public Atom getOutgoing(int index) {
        if (index < 0 || index >= outgoing.size()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for outgoing list of size " + outgoing.size());
        }
        return outgoing.get(index);
    }
    
    @Override
    public int getArity() {
        return outgoing.size();
    }
    
    /**
     * Check if this Link contains the given atom in its outgoing set
     */
    public boolean contains(Atom atom) {
        return outgoing.contains(atom);
    }
    
    /**
     * Find the index of an atom in the outgoing set
     */
    public int indexOf(Atom atom) {
        return outgoing.indexOf(atom);
    }
    
    @Override
    protected int computeHashCode() {
        return Objects.hash(getType(), outgoing);
    }
    
    @Override
    protected boolean contentEquals(Atom other) {
        if (!(other instanceof Link)) return false;
        Link otherLink = (Link) other;
        return getType() == otherLink.getType() && 
               Objects.equals(outgoing, otherLink.outgoing);
    }
    
    @Override
    protected void appendContent(StringBuilder sb) {
        for (Atom atom : outgoing) {
            sb.append(" ").append(atom);
        }
    }
    
    @Override
    public String toDebugString() {
        return String.format("Link[id=%d, type=%s, arity=%d, incoming=%d, values=%d]",
                           getId(), getType(), getArity(), getIncomingSetSize(), getAllValues().size());
    }
    
    // Factory methods for common Link types
    
    public static Link list(Atom... atoms) {
        return new Link(AtomType.LIST_LINK, atoms);
    }
    
    public static Link evaluation(Atom predicate, Atom... args) {
        List<Atom> outgoing = new ArrayList<>();
        outgoing.add(predicate);
        if (args.length == 1) {
            outgoing.add(args[0]);
        } else {
            outgoing.add(list(args));
        }
        return new Link(AtomType.EVALUATION_LINK, outgoing);
    }
    
    public static Link inheritance(Atom subclass, Atom superclass) {
        return new Link(AtomType.INHERITANCE_LINK, subclass, superclass);
    }
    
    public static Link similarity(Atom atom1, Atom atom2) {
        return new Link(AtomType.SIMILARITY_LINK, atom1, atom2);
    }
    
    public static Link and(Atom... atoms) {
        return new Link(AtomType.AND_LINK, atoms);
    }
    
    public static Link or(Atom... atoms) {
        return new Link(AtomType.OR_LINK, atoms);
    }
    
    public static Link not(Atom atom) {
        return new Link(AtomType.NOT_LINK, atom);
    }
    
    public static Link plus(Atom... atoms) {
        return new Link(AtomType.PLUS_LINK, atoms);
    }
    
    public static Link times(Atom... atoms) {
        return new Link(AtomType.TIMES_LINK, atoms);
    }
    
    public static Link greaterThan(Atom left, Atom right) {
        return new Link(AtomType.GREATER_THAN_LINK, left, right);
    }
}