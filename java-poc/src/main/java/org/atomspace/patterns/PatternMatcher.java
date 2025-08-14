package org.atomspace.patterns;

import org.atomspace.core.*;
import org.atomspace.types.AtomType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PatternMatcher implements the core pattern matching engine for AtomSpace.
 * 
 * This is one of the most sophisticated features of AtomSpace - the ability to
 * search for graph patterns with variables and perform complex queries.
 * 
 * Key features:
 * 1. Variable binding and substitution
 * 2. Structural pattern matching in hypergraphs  
 * 3. Type constraints on variables
 * 4. Support for unification and pattern instantiation
 * 
 * This enables advanced AI reasoning, rule engines, and knowledge queries.
 */
public class PatternMatcher {
    
    private final AtomSpace atomSpace;
    
    public PatternMatcher(AtomSpace atomSpace) {
        this.atomSpace = atomSpace;
    }
    
    /**
     * Find all groundings (variable bindings) that match the given pattern.
     * 
     * @param pattern The pattern to match (may contain variables)
     * @return List of variable bindings that satisfy the pattern
     */
    public List<Map<String, Atom>> findMatches(Atom pattern) {
        Set<String> variables = extractVariables(pattern);
        List<Map<String, Atom>> results = new ArrayList<>();
        
        if (variables.isEmpty()) {
            // No variables - just check if pattern exists in atomspace
            if (atomSpace.contains(pattern)) {
                results.add(new HashMap<>());
            }
            return results;
        }
        
        // Generate all possible variable bindings and test each one
        List<Map<String, Atom>> possibleBindings = generatePossibleBindings(variables);
        
        for (Map<String, Atom> binding : possibleBindings) {
            Atom instantiated = instantiatePattern(pattern, binding);
            if (instantiated != null && atomSpace.contains(instantiated)) {
                results.add(new HashMap<>(binding));
            }
        }
        
        return results;
    }
    
    /**
     * Extract all variable names from a pattern
     */
    private Set<String> extractVariables(Atom pattern) {
        Set<String> variables = new HashSet<>();
        extractVariablesRecursive(pattern, variables);
        return variables;
    }
    
    private void extractVariablesRecursive(Atom atom, Set<String> variables) {
        if (atom instanceof Node) {
            Node node = (Node) atom;
            if (node.getType() == AtomType.VARIABLE_NODE) {
                variables.add(node.getName());
            }
        } else if (atom instanceof Link) {
            Link link = (Link) atom;
            for (Atom outgoing : link.getOutgoing()) {
                extractVariablesRecursive(outgoing, variables);
            }
        }
    }
    
    /**
     * Generate all possible variable bindings from the atomspace
     */
    private List<Map<String, Atom>> generatePossibleBindings(Set<String> variables) {
        List<Map<String, Atom>> bindings = new ArrayList<>();
        
        // Start with empty binding
        Map<String, Atom> emptyBinding = new HashMap<>();
        bindings.add(emptyBinding);
        
        // For each variable, extend all existing bindings
        for (String variable : variables) {
            List<Map<String, Atom>> newBindings = new ArrayList<>();
            
            // Get all possible atoms that could bind to this variable
            Collection<Atom> candidates = getCandidatesForVariable(variable);
            
            for (Map<String, Atom> binding : bindings) {
                for (Atom candidate : candidates) {
                    Map<String, Atom> newBinding = new HashMap<>(binding);
                    newBinding.put(variable, candidate);
                    newBindings.add(newBinding);
                }
            }
            bindings = newBindings;
            
            // Limit to prevent combinatorial explosion
            if (bindings.size() > 10000) {
                bindings = bindings.subList(0, 10000);
            }
        }
        
        return bindings;
    }
    
    /**
     * Get candidate atoms that could bind to a variable
     */
    private Collection<Atom> getCandidatesForVariable(String variableName) {
        // For now, return all atoms - could be optimized with type constraints
        Collection<Atom> candidates = atomSpace.getAllAtoms();
        
        // Filter out variables to avoid recursive bindings
        return candidates.stream()
                .filter(atom -> !(atom instanceof Node && 
                                ((Node) atom).getType() == AtomType.VARIABLE_NODE))
                .collect(Collectors.toList());
    }
    
    /**
     * Instantiate a pattern by replacing variables with their bindings
     */
    public Atom instantiatePattern(Atom pattern, Map<String, Atom> bindings) {
        if (pattern instanceof Node) {
            Node node = (Node) pattern;
            if (node.getType() == AtomType.VARIABLE_NODE && bindings.containsKey(node.getName())) {
                return bindings.get(node.getName());
            }
            return pattern;
        } else if (pattern instanceof Link) {
            Link link = (Link) pattern;
            List<Atom> newOutgoing = new ArrayList<>();
            
            for (Atom outgoing : link.getOutgoing()) {
                Atom instantiated = instantiatePattern(outgoing, bindings);
                if (instantiated == null) return null;
                newOutgoing.add(instantiated);
            }
            
            return new Link(link.getType(), newOutgoing);
        }
        
        return pattern;
    }
    
    /**
     * Simple query: find all atoms that match a pattern
     */
    public Set<Atom> queryAtoms(Atom pattern) {
        List<Map<String, Atom>> matches = findMatches(pattern);
        return matches.stream()
                .map(binding -> instantiatePattern(pattern, binding))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get all atoms of a certain type that are related to a given atom
     */
    public Set<Atom> getRelated(Atom target, AtomType relationType, boolean incoming) {
        Set<Atom> related = new HashSet<>();
        
        if (incoming) {
            // Look for atoms that point to the target
            for (Atom atom : target.getIncomingSet()) {
                if (atom.getType() == relationType) {
                    related.add(atom);
                }
            }
        } else {
            // Look for atoms that the target points to (if it's a link)
            if (target instanceof Link) {
                Link link = (Link) target;
                if (link.getType() == relationType) {
                    related.addAll(link.getOutgoing());
                }
            }
        }
        
        return related;
    }
    
    /**
     * Find all atoms that inherit from a given concept
     */
    public Set<Atom> getSubclasses(Atom concept) {
        Set<Atom> subclasses = new HashSet<>();
        
        for (Atom atom : concept.getIncomingSet()) {
            if (atom instanceof Link) {
                Link link = (Link) atom;
                if (link.getType() == AtomType.INHERITANCE_LINK && 
                    link.getArity() == 2 && 
                    link.getOutgoing(1).equals(concept)) {
                    subclasses.add(link.getOutgoing(0));
                }
            }
        }
        
        return subclasses;
    }
    
    /**
     * Find all atoms that are similar to a given atom
     */
    public Set<Atom> getSimilar(Atom atom, double minStrength) {
        Set<Atom> similar = new HashSet<>();
        
        for (Atom incoming : atom.getIncomingSet()) {
            if (incoming instanceof Link) {
                Link link = (Link) incoming;
                if (link.getType() == AtomType.SIMILARITY_LINK && 
                    link.getArity() == 2 &&
                    link.getTruthValue().getStrength() >= minStrength) {
                    
                    // Add the other atom in the similarity link
                    if (link.getOutgoing(0).equals(atom)) {
                        similar.add(link.getOutgoing(1));
                    } else if (link.getOutgoing(1).equals(atom)) {
                        similar.add(link.getOutgoing(0));
                    }
                }
            }
        }
        
        return similar;
    }
    
    /**
     * Traverse the graph starting from a given atom
     */
    public Set<Atom> traverse(Atom start, int maxDepth) {
        Set<Atom> visited = new HashSet<>();
        Queue<Atom> queue = new LinkedList<>();
        Map<Atom, Integer> depths = new HashMap<>();
        
        queue.offer(start);
        depths.put(start, 0);
        
        while (!queue.isEmpty()) {
            Atom current = queue.poll();
            visited.add(current);
            
            int currentDepth = depths.get(current);
            if (currentDepth >= maxDepth) continue;
            
            // Add outgoing atoms (if it's a link)
            if (current instanceof Link) {
                Link link = (Link) current;
                for (Atom outgoing : link.getOutgoing()) {
                    if (!visited.contains(outgoing) && !depths.containsKey(outgoing)) {
                        queue.offer(outgoing);
                        depths.put(outgoing, currentDepth + 1);
                    }
                }
            }
            
            // Add incoming atoms
            for (Atom incoming : current.getIncomingSet()) {
                if (!visited.contains(incoming) && !depths.containsKey(incoming)) {
                    queue.offer(incoming);
                    depths.put(incoming, currentDepth + 1);
                }
            }
        }
        
        return visited;
    }
}