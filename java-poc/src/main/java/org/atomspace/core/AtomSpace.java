package org.atomspace.core;

import org.atomspace.types.AtomType;
import org.atomspace.values.TruthValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AtomSpace is the core hypergraph database that stores and indexes Atoms.
 * 
 * Key features that make AtomSpace unique among graph databases:
 * 
 * 1. HYPERGRAPH STORAGE: More efficient than vertex+edge storage for complex graphs
 * 2. GLOBAL UNIQUENESS: Ensures only one instance of any given atom exists
 * 3. TYPE-BASED INDEXING: Efficient retrieval by atom type
 * 4. INCOMING SET TRACKING: Bidirectional graph traversal
 * 5. PATTERN MATCHING: Advanced query capabilities beyond simple graph traversal
 * 6. MUTABLE VALUES: Dynamic data flows through immutable graph structure
 * 
 * This design enables more sophisticated AI reasoning compared to traditional databases.
 */
public class AtomSpace {
    
    // Global atom registry - ensures uniqueness
    private final ConcurrentHashMap<Integer, Atom> atomRegistry;
    
    // Type-based indexes for efficient lookup
    private final ConcurrentHashMap<AtomType, Set<Atom>> typeIndex;
    
    // Name-based index for nodes
    private final ConcurrentHashMap<String, Set<Node>> nameIndex;
    
    // Statistics
    private volatile long nodeCount;
    private volatile long linkCount;
    
    public AtomSpace() {
        this.atomRegistry = new ConcurrentHashMap<>();
        this.typeIndex = new ConcurrentHashMap<>();
        this.nameIndex = new ConcurrentHashMap<>();
        this.nodeCount = 0;
        this.linkCount = 0;
    }
    
    /**
     * Add an atom to this AtomSpace, ensuring global uniqueness.
     * If an equivalent atom already exists, return the existing one.
     */
    public synchronized <T extends Atom> T add(T atom) {
        if (atom == null) return null;
        
        // Check if equivalent atom already exists
        T existing = findEquivalent(atom);
        if (existing != null) {
            return existing;
        }
        
        // Add new atom
        atomRegistry.put(atom.hashCode(), atom);
        
        // Update type index
        typeIndex.computeIfAbsent(atom.getType(), k -> ConcurrentHashMap.newKeySet()).add(atom);
        
        // Update name index for nodes
        if (atom instanceof Node) {
            Node node = (Node) atom;
            if (!node.getName().isEmpty()) {
                nameIndex.computeIfAbsent(node.getName(), k -> ConcurrentHashMap.newKeySet()).add(node);
            }
            nodeCount++;
        } else {
            linkCount++;
        }
        
        return atom;
    }
    
    /**
     * Find an equivalent atom in the AtomSpace
     */
    @SuppressWarnings("unchecked")
    private <T extends Atom> T findEquivalent(T atom) {
        Atom existing = atomRegistry.get(atom.hashCode());
        if (existing != null && existing.equals(atom)) {
            return (T) existing;
        }
        return null;
    }
    
    /**
     * Remove an atom from this AtomSpace
     */
    public synchronized boolean remove(Atom atom) {
        if (atom == null) return false;
        
        Atom removed = atomRegistry.remove(atom.hashCode());
        if (removed == null) return false;
        
        // Update type index
        Set<Atom> typeSet = typeIndex.get(atom.getType());
        if (typeSet != null) {
            typeSet.remove(atom);
            if (typeSet.isEmpty()) {
                typeIndex.remove(atom.getType());
            }
        }
        
        // Update name index for nodes
        if (atom instanceof Node) {
            Node node = (Node) atom;
            if (!node.getName().isEmpty()) {
                Set<Node> nameSet = nameIndex.get(node.getName());
                if (nameSet != null) {
                    nameSet.remove(node);
                    if (nameSet.isEmpty()) {
                        nameIndex.remove(node.getName());
                    }
                }
            }
            nodeCount--;
        } else {
            linkCount--;
        }
        
        return true;
    }
    
    /**
     * Check if this AtomSpace contains the given atom
     */
    public boolean contains(Atom atom) {
        if (atom == null) return false;
        return atomRegistry.containsKey(atom.hashCode()) && 
               atomRegistry.get(atom.hashCode()).equals(atom);
    }
    
    /**
     * Get all atoms in this AtomSpace
     */
    public Collection<Atom> getAllAtoms() {
        return new ArrayList<>(atomRegistry.values());
    }
    
    /**
     * Get all atoms of a specific type
     */
    public Set<Atom> getAtomsByType(AtomType type) {
        Set<Atom> atoms = typeIndex.get(type);
        return atoms != null ? new HashSet<>(atoms) : new HashSet<>();
    }
    
    /**
     * Get all atoms of a specific type or its subtypes
     */
    public Set<Atom> getAtomsByTypeRecursive(AtomType type) {
        Set<Atom> result = new HashSet<>();
        for (AtomType atomType : AtomType.values()) {
            if (atomType.isSubtypeOf(type)) {
                result.addAll(getAtomsByType(atomType));
            }
        }
        return result;
    }
    
    /**
     * Get nodes by name
     */
    public Set<Node> getNodesByName(String name) {
        Set<Node> nodes = nameIndex.get(name);
        return nodes != null ? new HashSet<>(nodes) : new HashSet<>();
    }
    
    /**
     * Get nodes by name and type
     */
    public Set<Node> getNodesByNameAndType(String name, AtomType type) {
        return getNodesByName(name).stream()
                .filter(node -> node.getType() == type)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get or create a node with the given type and name
     */
    public Node getOrCreateNode(AtomType type, String name) {
        // Look for existing node
        Set<Node> existing = getNodesByNameAndType(name, type);
        if (!existing.isEmpty()) {
            return existing.iterator().next();
        }
        
        // Create new node
        return add(new Node(type, name));
    }
    
    /**
     * Get or create a link with the given type and outgoing atoms
     */
    public Link getOrCreateLink(AtomType type, Atom... outgoing) {
        return add(new Link(type, outgoing));
    }
    
    /**
     * Get all atoms with a specific truth value strength above threshold
     */
    public Set<Atom> getAtomsByTruthValueStrength(double minStrength) {
        return atomRegistry.values().stream()
                .filter(atom -> atom.getTruthValue().getStrength() >= minStrength)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get atoms that have incoming atoms (are referenced by others)
     */
    public Set<Atom> getAtomsWithIncoming() {
        return atomRegistry.values().stream()
                .filter(Atom::hasIncomingAtoms)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get atoms that have no incoming atoms (unreferenced)
     */
    public Set<Atom> getAtomsWithoutIncoming() {
        return atomRegistry.values().stream()
                .filter(atom -> !atom.hasIncomingAtoms())
                .collect(Collectors.toSet());
    }
    
    /**
     * Get the size of this AtomSpace
     */
    public long size() {
        return atomRegistry.size();
    }
    
    /**
     * Get the number of nodes
     */
    public long getNodeCount() {
        return nodeCount;
    }
    
    /**
     * Get the number of links
     */
    public long getLinkCount() {
        return linkCount;
    }
    
    /**
     * Check if this AtomSpace is empty
     */
    public boolean isEmpty() {
        return atomRegistry.isEmpty();
    }
    
    /**
     * Clear all atoms from this AtomSpace
     */
    public synchronized void clear() {
        atomRegistry.clear();
        typeIndex.clear();
        nameIndex.clear();
        nodeCount = 0;
        linkCount = 0;
    }
    
    /**
     * Get statistics about this AtomSpace
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAtoms", size());
        stats.put("nodes", getNodeCount());
        stats.put("links", getLinkCount());
        stats.put("types", typeIndex.size());
        stats.put("namedNodes", nameIndex.size());
        
        // Type distribution
        Map<AtomType, Integer> typeCounts = new HashMap<>();
        for (Map.Entry<AtomType, Set<Atom>> entry : typeIndex.entrySet()) {
            typeCounts.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("typeDistribution", typeCounts);
        
        return stats;
    }
    
    @Override
    public String toString() {
        return String.format("AtomSpace[atoms=%d, nodes=%d, links=%d, types=%d]", 
                           size(), getNodeCount(), getLinkCount(), typeIndex.size());
    }
    
    // Convenience factory methods that automatically add to AtomSpace
    
    public Node concept(String name) {
        return add(Node.concept(name));
    }
    
    public Node predicate(String name) {
        return add(Node.predicate(name));
    }
    
    public Node variable(String name) {
        return add(Node.variable(name));
    }
    
    public Link list(Atom... atoms) {
        return add(Link.list(atoms));
    }
    
    public Link evaluation(Atom predicate, Atom... args) {
        return add(Link.evaluation(predicate, args));
    }
    
    public Link inheritance(Atom subclass, Atom superclass) {
        return add(Link.inheritance(subclass, superclass));
    }
    
    public Link similarity(Atom atom1, Atom atom2) {
        return add(Link.similarity(atom1, atom2));
    }
}