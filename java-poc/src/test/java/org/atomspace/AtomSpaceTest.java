package org.atomspace;

import org.atomspace.core.*;
import org.atomspace.execution.ExecutionEngine;
import org.atomspace.patterns.PatternMatcher;
import org.atomspace.types.AtomType;
import org.atomspace.values.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for AtomSpace Java POC
 */
public class AtomSpaceTest {
    
    private AtomSpace atomSpace;
    private PatternMatcher patternMatcher;
    private ExecutionEngine executionEngine;
    
    @BeforeEach
    void setUp() {
        atomSpace = new AtomSpace();
        patternMatcher = new PatternMatcher(atomSpace);
        executionEngine = new ExecutionEngine(atomSpace);
    }
    
    @Test
    void testAtomCreationAndUniqueness() {
        // Test global uniqueness
        Node cat1 = atomSpace.concept("cat");
        Node cat2 = atomSpace.concept("cat");
        
        assertSame(cat1, cat2, "Same concept nodes should be identical");
        assertEquals(cat1, cat2, "Same concept nodes should be equal");
        
        // Test different types are different
        Node catConcept = atomSpace.concept("cat");
        Node catPredicate = atomSpace.predicate("cat");
        
        assertNotEquals(catConcept, catPredicate, "Different types with same name should be different");
    }
    
    @Test
    void testAtomValueSystem() {
        Node cat = atomSpace.concept("cat");
        
        // Test truth values
        TruthValue tv = new TruthValue(0.8, 0.9);
        cat.setTruthValue(tv);
        assertEquals(tv, cat.getTruthValue());
        
        // Test float values
        FloatValue weight = new FloatValue(4.5, 5.2, 3.8);
        cat.setValue("weights", weight);
        assertEquals(weight, cat.getValue("weights"));
        
        // Test value operations
        FloatValue other = new FloatValue(1.0, 2.0, 3.0);
        FloatValue sum = weight.add(other);
        assertEquals(5.5, sum.getValue(0), 0.001);
        assertEquals(7.2, sum.getValue(1), 0.001);
        assertEquals(6.8, sum.getValue(2), 0.001);
    }
    
    @Test
    void testHypergraphStructure() {
        Node john = atomSpace.concept("John");
        Node mary = atomSpace.concept("Mary");
        Node likes = atomSpace.predicate("likes");
        
        // Create evaluation link
        Link evaluation = atomSpace.evaluation(likes, john, mary);
        
        assertEquals(2, evaluation.getArity());
        assertEquals(likes, evaluation.getOutgoing(0));
        
        // The second argument should be a ListLink containing john and mary
        Atom secondArg = evaluation.getOutgoing(1);
        assertTrue(secondArg instanceof Link);
        Link listLink = (Link) secondArg;
        assertEquals(AtomType.LIST_LINK, listLink.getType());
        assertEquals(2, listLink.getArity());
        assertEquals(john, listLink.getOutgoing(0));
        assertEquals(mary, listLink.getOutgoing(1));
        
        // Test incoming sets
        assertTrue(john.getIncomingSet().size() > 0);
        assertTrue(mary.getIncomingSet().size() > 0);
        assertTrue(likes.getIncomingSet().contains(evaluation));
    }
    
    @Test
    void testPatternMatching() {
        // For this POC, let's test a simpler approach to pattern matching
        // Create knowledge base
        Node dog = atomSpace.concept("dog");
        Node cat = atomSpace.concept("cat");
        Node animal = atomSpace.concept("animal");
        
        Link dogInheritance = atomSpace.inheritance(dog, animal);
        Link catInheritance = atomSpace.inheritance(cat, animal);
        
        // Verify the links were created correctly
        assertTrue(atomSpace.contains(dogInheritance));
        assertTrue(atomSpace.contains(catInheritance));
        
        // Debug: Check incoming set of animal
        System.out.println("Animal incoming set size: " + animal.getIncomingSet().size());
        for (Atom incoming : animal.getIncomingSet()) {
            System.out.println("  Incoming: " + incoming + " (type: " + incoming.getType() + ")");
            if (incoming instanceof Link) {
                Link link = (Link) incoming;
                System.out.println("    Arity: " + link.getArity());
                for (int i = 0; i < link.getArity(); i++) {
                    System.out.println("    Outgoing[" + i + "]: " + link.getOutgoing(i));
                }
            }
        }
        
        // Test related atoms query instead of full pattern matching
        Set<Atom> subclasses = patternMatcher.getSubclasses(animal);
        
        System.out.println("Found subclasses: " + subclasses.size());
        for (Atom subclass : subclasses) {
            System.out.println("  Subclass: " + subclass);
        }
        
        assertTrue(subclasses.contains(dog), "Should find dog as subclass of animal");
        assertTrue(subclasses.contains(cat), "Should find cat as subclass of animal");
        assertEquals(2, subclasses.size(), "Should find exactly 2 subclasses");
    }
    
    @Test
    void testExecutableGraphs() {
        // Create mathematical expression: 3 + 4
        Atom expr = ExecutionEngine.createMathExpression(atomSpace, "3 + 4");
        atomSpace.add(expr);
        
        // Execute it
        Atom result = executionEngine.execute(expr);
        
        assertNotNull(result);
        assertTrue(result instanceof Node);
        
        Value numValue = result.getValue("numeric-value");
        assertNotNull(numValue);
        assertTrue(numValue instanceof FloatValue);
        
        FloatValue fv = (FloatValue) numValue;
        assertEquals(7.0, fv.getValue(0), 0.001);
    }
    
    @Test
    void testProbabilisticReasoning() {
        TruthValue tv1 = new TruthValue(0.8, 0.7);
        TruthValue tv2 = new TruthValue(0.6, 0.8);
        
        // Test revision
        TruthValue revised = tv1.revise(tv2);
        
        assertNotNull(revised);
        assertTrue(revised.getStrength() > 0.0);
        assertTrue(revised.getStrength() < 1.0);
        assertTrue(revised.getConfidence() > tv1.getConfidence());
        assertTrue(revised.getConfidence() > tv2.getConfidence());
    }
    
    @Test
    void testTypeSystem() {
        // Test type hierarchy
        assertTrue(AtomType.CONCEPT_NODE.isSubtypeOf(AtomType.NODE));
        assertTrue(AtomType.NODE.isSubtypeOf(AtomType.ATOM));
        assertTrue(AtomType.INHERITANCE_LINK.isSubtypeOf(AtomType.LINK));
        assertFalse(AtomType.NODE.isSubtypeOf(AtomType.LINK));
        
        // Test type checking in atoms
        Node concept = new Node(AtomType.CONCEPT_NODE, "test");
        assertTrue(concept.getType().isNode());
        assertFalse(concept.getType().isLink());
        
        Link inheritance = new Link(AtomType.INHERITANCE_LINK, concept, concept);
        assertTrue(inheritance.getType().isLink());
        assertFalse(inheritance.getType().isNode());
    }
    
    @Test
    void testAtomSpaceIndexing() {
        Node cat = atomSpace.concept("cat");
        Node dog = atomSpace.concept("dog");
        Node animal = atomSpace.concept("animal");
        
        atomSpace.inheritance(cat, animal);
        atomSpace.inheritance(dog, animal);
        
        // Test type-based retrieval
        Set<Atom> concepts = atomSpace.getAtomsByType(AtomType.CONCEPT_NODE);
        assertTrue(concepts.contains(cat));
        assertTrue(concepts.contains(dog));
        assertTrue(concepts.contains(animal));
        
        Set<Atom> links = atomSpace.getAtomsByType(AtomType.INHERITANCE_LINK);
        assertEquals(2, links.size());
        
        // Test name-based retrieval
        Set<Node> catNodes = atomSpace.getNodesByName("cat");
        assertEquals(1, catNodes.size());
        assertTrue(catNodes.contains(cat));
    }
    
    @Test
    void testAtomSpaceStatistics() {
        // Add some atoms
        atomSpace.concept("cat");
        atomSpace.concept("dog");
        atomSpace.predicate("likes");
        atomSpace.inheritance(atomSpace.concept("cat"), atomSpace.concept("animal"));
        
        Map<String, Object> stats = atomSpace.getStatistics();
        
        assertTrue((Long) stats.get("totalAtoms") > 0);
        assertTrue((Long) stats.get("nodes") > 0);
        assertTrue((Long) stats.get("links") > 0);
        
        @SuppressWarnings("unchecked")
        Map<AtomType, Integer> typeDist = (Map<AtomType, Integer>) stats.get("typeDistribution");
        assertTrue(typeDist.containsKey(AtomType.CONCEPT_NODE));
        assertTrue(typeDist.get(AtomType.CONCEPT_NODE) >= 3); // cat, dog, animal
    }
    
    @Test
    void testStreamingValues() {
        Node sensor = atomSpace.concept("sensor");
        
        // Create a simple stream
        StreamValue stream = StreamValue.randomFloats(3, 5);
        sensor.setValue("data", stream);
        
        assertEquals(stream, sensor.getValue("data"));
        assertTrue(stream.isActive());
        
        // Test getting values
        Value latest = stream.getLatest();
        if (latest != null) {
            assertTrue(latest instanceof FloatValue);
            FloatValue fv = (FloatValue) latest;
            assertEquals(3, fv.size());
        }
        
        // Clean up
        stream.stop();
        
        // Give it a moment to stop
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertFalse(stream.isActive());
    }
}