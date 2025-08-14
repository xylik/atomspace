package org.atomspace.core;

import org.atomspace.types.AtomType;

import java.util.Objects;

/**
 * Node represents atomic concepts with string names.
 * 
 * Nodes are the leaf atoms in the hypergraph - they have no outgoing connections.
 * They are identified by their type and name string. Examples:
 * 
 * - (ConceptNode "cat") - represents the concept of a cat
 * - (PredicateNode "likes") - represents the relation "likes" 
 * - (VariableNode "$X") - represents a variable in pattern matching
 * 
 * This corresponds to vertices in traditional graph databases, but with
 * the key difference that Nodes are immutable and globally unique.
 */
public class Node extends Atom {
    
    private final String name;
    
    /**
     * Create a new Node with the given type and name
     */
    public Node(AtomType type, String name) {
        super(type);
        if (!type.isNode()) {
            throw new IllegalArgumentException("AtomType " + type + " is not a Node type");
        }
        this.name = name != null ? name : "";
    }
    
    /**
     * Get the name of this Node
     */
    public String getName() {
        return name;
    }
    
    /**
     * Check if this is an unnamed node (empty string name)
     */
    public boolean isUnnamed() {
        return name.isEmpty();
    }
    
    @Override
    public int getArity() {
        return 0; // Nodes have no outgoing connections
    }
    
    @Override
    protected int computeHashCode() {
        return Objects.hash(getType(), name);
    }
    
    @Override
    protected boolean contentEquals(Atom other) {
        if (!(other instanceof Node)) return false;
        Node otherNode = (Node) other;
        return getType() == otherNode.getType() && 
               Objects.equals(name, otherNode.name);
    }
    
    @Override
    protected void appendContent(StringBuilder sb) {
        if (!name.isEmpty()) {
            sb.append(" \"").append(name).append("\"");
        }
    }
    
    @Override
    public String toDebugString() {
        return String.format("Node[id=%d, type=%s, name=\"%s\", incoming=%d, values=%d]",
                           getId(), getType(), name, getIncomingSetSize(), getAllValues().size());
    }
    
    // Factory methods for common Node types
    
    public static Node concept(String name) {
        return new Node(AtomType.CONCEPT_NODE, name);
    }
    
    public static Node predicate(String name) {
        return new Node(AtomType.PREDICATE_NODE, name);
    }
    
    public static Node variable(String name) {
        return new Node(AtomType.VARIABLE_NODE, name);
    }
    
    public static Node schema(String name) {
        return new Node(AtomType.SCHEMA_NODE, name);
    }
    
    public static Node type(String name) {
        return new Node(AtomType.TYPE_NODE, name);
    }
}