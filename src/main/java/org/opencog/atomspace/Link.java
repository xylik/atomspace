package org.opencog.atomspace;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Java representation of a Link in the AtomSpace.
 * Links are composite structures that contain other Atoms.
 */
public class Link extends Atom {
    private final List<Atom> outgoing;
    
    public Link(String type, List<Atom> outgoing) {
        super(type, computeLinkKey(type, outgoing));
        this.outgoing = List.copyOf(outgoing); // immutable copy
    }
    
    public List<Atom> getOutgoing() {
        return outgoing;
    }
    
    public int getArity() {
        return outgoing.size();
    }
    
    /**
     * Compute structural key for a Link.
     * Format: "L|<type>|<arity>|<child1_key>|<child2_key>|..."
     */
    private static String computeLinkKey(String type, List<Atom> outgoing) {
        StringBuilder sb = new StringBuilder();
        sb.append("L|").append(type).append("|").append(outgoing.size());
        
        for (Atom child : outgoing) {
            sb.append("|").append(child.getStructuralKey());
        }
        
        return sb.toString();
    }
    
    @Override
    public String computeStructuralKey() {
        return computeLinkKey(getType(), outgoing);
    }
    
    @Override
    public String toAtomese() {
        if (outgoing.isEmpty()) {
            return String.format("(%s)", getType());
        }
        
        String children = outgoing.stream()
            .map(Atom::toAtomese)
            .collect(Collectors.joining(" "));
        
        return String.format("(%s %s)", getType(), children);
    }
    
    @Override
    public String toString() {
        return String.format("Link[%s:%d]", getType(), outgoing.size());
    }
}