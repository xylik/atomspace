package org.opencog.atomspace;

/**
 * Java representation of a Node in the AtomSpace.
 * Nodes are atomic (leaf) elements with a type and name.
 */
public class Node extends Atom {
    private final String name;
    
    public Node(String type, String name) {
        super(type, computeNodeKey(type, name));
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Compute structural key for a Node.
     * Format: "N|<type>|<name>"
     */
    private static String computeNodeKey(String type, String name) {
        return String.format("N|%s|%s", type, name);
    }
    
    @Override
    public String computeStructuralKey() {
        return computeNodeKey(getType(), name);
    }
    
    @Override
    public String toAtomese() {
        return String.format("(%s \"%s\")", getType(), name);
    }
    
    @Override
    public String toString() {
        return String.format("Node[%s:%s]", getType(), name);
    }
}