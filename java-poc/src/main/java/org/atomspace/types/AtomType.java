package org.atomspace.types;

/**
 * Type system for AtomSpace - represents the type of an Atom.
 * 
 * In the original AtomSpace, types form a hierarchy and can have type constructors
 * similar to functional programming languages like Haskell. This simplified version
 * demonstrates the core concept.
 */
public enum AtomType {
    // Basic types
    ATOM("Atom"),
    NODE("Node"), 
    LINK("Link"),
    
    // Specific Node types
    CONCEPT_NODE("ConceptNode"),
    PREDICATE_NODE("PredicateNode"), 
    VARIABLE_NODE("VariableNode"),
    SCHEMA_NODE("SchemaNode"),
    TYPE_NODE("TypeNode"),
    
    // Specific Link types  
    LIST_LINK("ListLink"),
    EVALUATION_LINK("EvaluationLink"),
    INHERITANCE_LINK("InheritanceLink"),
    SIMILARITY_LINK("SimilarityLink"),
    
    // Pattern matching links
    BIND_LINK("BindLink"),
    GET_LINK("GetLink"),
    QUERY_LINK("QueryLink"),
    
    // Logical links
    AND_LINK("AndLink"),
    OR_LINK("OrLink"),
    NOT_LINK("NotLink"),
    
    // Execution/computation links
    EXECUTION_OUTPUT_LINK("ExecutionOutputLink"),
    PLUS_LINK("PlusLink"),
    TIMES_LINK("TimesLink"),
    GREATER_THAN_LINK("GreaterThanLink"),
    
    // Special pattern matching atoms
    CHOICE_LINK("ChoiceLink"),
    UNORDERED_LINK("UnorderedLink"),
    ABSENT_LINK("AbsentLink"),
    ALWAYS_LINK("AlwaysLink"),
    QUOTE_LINK("QuoteLink");
    
    private final String name;
    
    AtomType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Check if this type is a subtype of another type.
     * This implements a simplified type hierarchy.
     */
    public boolean isSubtypeOf(AtomType other) {
        if (this == other) return true;
        
        // All atoms are subtypes of ATOM
        if (other == ATOM) return true;
        
        // All nodes are subtypes of NODE
        if (other == NODE) {
            return this == CONCEPT_NODE || this == PREDICATE_NODE || 
                   this == VARIABLE_NODE || this == SCHEMA_NODE || this == TYPE_NODE;
        }
        
        // All links are subtypes of LINK
        if (other == LINK) {
            return !isSubtypeOf(NODE) && this != ATOM;
        }
        
        return false;
    }
    
    /**
     * Check if this is a Node type
     */
    public boolean isNode() {
        return isSubtypeOf(NODE);
    }
    
    /**
     * Check if this is a Link type
     */
    public boolean isLink() {
        return isSubtypeOf(LINK);
    }
    
    @Override
    public String toString() {
        return name;
    }
}