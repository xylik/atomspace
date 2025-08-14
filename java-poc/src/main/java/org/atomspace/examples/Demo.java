package org.atomspace.examples;

import org.atomspace.core.*;
import org.atomspace.execution.ExecutionEngine;
import org.atomspace.patterns.PatternMatcher;
import org.atomspace.types.AtomType;
import org.atomspace.values.*;

import java.util.*;

/**
 * Comprehensive demonstration of AtomSpace key technical ideas.
 * 
 * This demo showcases the innovations that make AtomSpace unique:
 * 1. Immutable Atoms vs Mutable Values architecture
 * 2. Hypergraph storage with global uniqueness  
 * 3. Advanced pattern matching with variables
 * 4. Executable graphs (Atomese) 
 * 5. Rich type system
 * 6. Probabilistic reasoning with TruthValues
 * 7. Streaming data through Values
 */
public class Demo {
    
    public static void main(String[] args) {
        System.out.println("=== AtomSpace Java POC Demo ===\n");
        
        AtomSpace atomSpace = new AtomSpace();
        PatternMatcher patternMatcher = new PatternMatcher(atomSpace);
        ExecutionEngine executionEngine = new ExecutionEngine(atomSpace);
        
        // Demonstrate each key technical idea
        demonstrateAtomValueDistinction(atomSpace);
        demonstrateHypergraphStorage(atomSpace);
        demonstrateTypeSystem(atomSpace);
        demonstratePatternMatching(atomSpace, patternMatcher);
        demonstrateExecutableGraphs(atomSpace, executionEngine);
        demonstrateProbabilisticReasoning(atomSpace);
        demonstrateStreamingValues(atomSpace);
        demonstrateAdvancedQueries(atomSpace, patternMatcher);
        
        printStatistics(atomSpace);
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    /**
     * Demonstrate the Atom/Value distinction - core architectural innovation
     */
    private static void demonstrateAtomValueDistinction(AtomSpace atomSpace) {
        System.out.println("1. ATOM/VALUE DISTINCTION");
        System.out.println("Atoms are immutable graph structure, Values are mutable data\n");
        
        // Create immutable atoms
        Node cat = atomSpace.concept("cat");
        Node mammal = atomSpace.concept("mammal"); 
        Link inheritance = atomSpace.inheritance(cat, mammal);
        
        System.out.println("Created immutable atoms:");
        System.out.println("  " + cat);
        System.out.println("  " + inheritance);
        
        // Attach mutable values
        cat.setTruthValue(new TruthValue(0.9, 0.8));
        cat.setValue("weight", new FloatValue(4.5)); // kg
        cat.setValue("description", new StringValue("Domestic house cat"));
        
        // Create streaming data
        StreamValue temperatureStream = StreamValue.varyingTruth(0.7, 0.1, 10);
        cat.setValue("body-temperature", temperatureStream);
        
        System.out.println("\nAttached mutable values:");
        System.out.println("  Truth: " + cat.getTruthValue());
        System.out.println("  Weight: " + cat.getValue("weight"));
        System.out.println("  Description: " + cat.getValue("description"));
        System.out.println("  Temperature: " + cat.getValue("body-temperature"));
        
        // Show that atoms are globally unique
        Node cat2 = atomSpace.concept("cat");
        System.out.println("\nGlobal uniqueness: cat == cat2 ? " + (cat == cat2));
        
        System.out.println();
    }
    
    /**
     * Demonstrate hypergraph storage advantages
     */
    private static void demonstrateHypergraphStorage(AtomSpace atomSpace) {
        System.out.println("2. HYPERGRAPH STORAGE");
        System.out.println("Efficient storage of complex relationships as hyperedges\n");
        
        // Traditional graph would need multiple edges, AtomSpace uses one hyperedge
        Node john = atomSpace.concept("John");
        Node mary = atomSpace.concept("Mary");
        Node restaurant = atomSpace.concept("restaurant");
        Node pizza = atomSpace.concept("pizza");
        
        // Hyperedge: John and Mary went to restaurant to eat pizza
        Node went = atomSpace.predicate("went-to");
        Node eat = atomSpace.predicate("eat");
        
        // Complex relationship in one structure
        Link context = atomSpace.evaluation(went, 
            atomSpace.list(john, mary, restaurant));
        Link purpose = atomSpace.evaluation(eat,
            atomSpace.list(john, mary, pizza));
        
        // Meta-relationship: the context implies the purpose
        Link implication = new Link(AtomType.SIMILARITY_LINK, context, purpose);
        atomSpace.add(implication);
        implication.setTruthValue(new TruthValue(0.8, 0.9));
        
        System.out.println("Created complex hypergraph:");
        System.out.println("  " + context);
        System.out.println("  " + purpose);
        System.out.println("  " + implication);
        
        // Show bidirectional traversal
        System.out.println("\nIncoming links to 'John':");
        for (Atom incoming : john.getIncomingSet()) {
            System.out.println("  " + incoming.toDebugString());
        }
        
        System.out.println();
    }
    
    /**
     * Demonstrate rich type system
     */
    private static void demonstrateTypeSystem(AtomSpace atomSpace) {
        System.out.println("3. RICH TYPE SYSTEM");
        System.out.println("Hierarchical types with inheritance relationships\n");
        
        // Show type hierarchy
        Node animal = atomSpace.concept("Animal");
        Node mammal = atomSpace.concept("Mammal");
        Node cat = atomSpace.concept("Cat");
        
        atomSpace.inheritance(mammal, animal);
        atomSpace.inheritance(cat, mammal);
        
        // Different types of nodes and links
        Node variable = atomSpace.variable("$X");
        Node schema = new Node(AtomType.SCHEMA_NODE, "find-food");
        atomSpace.add(schema);
        
        System.out.println("Type hierarchy:");
        System.out.println("  Animal <- Mammal <- Cat");
        System.out.println("\nDifferent atom types:");
        System.out.println("  ConceptNode: " + cat);
        System.out.println("  VariableNode: " + variable);
        System.out.println("  SchemaNode: " + schema);
        System.out.println("  InheritanceLink: " + atomSpace.inheritance(cat, mammal));
        
        // Show type checking
        System.out.println("\nType checking:");
        System.out.println("  Is CONCEPT_NODE a NODE? " + AtomType.CONCEPT_NODE.isSubtypeOf(AtomType.NODE));
        System.out.println("  Is INHERITANCE_LINK a LINK? " + AtomType.INHERITANCE_LINK.isSubtypeOf(AtomType.LINK));
        
        System.out.println();
    }
    
    /**
     * Demonstrate advanced pattern matching
     */
    private static void demonstratePatternMatching(AtomSpace atomSpace, PatternMatcher patternMatcher) {
        System.out.println("4. ADVANCED PATTERN MATCHING");
        System.out.println("Graph queries with variables and complex patterns\n");
        
        // Add some knowledge
        Node dog = atomSpace.concept("dog");
        Node cat = atomSpace.concept("cat");
        Node bird = atomSpace.concept("bird");
        Node animal = atomSpace.concept("animal");
        
        atomSpace.inheritance(dog, animal);
        atomSpace.inheritance(cat, animal);
        atomSpace.inheritance(bird, animal);
        
        Node likes = atomSpace.predicate("likes");
        Node john = atomSpace.concept("john");
        atomSpace.evaluation(likes, john, dog);
        atomSpace.evaluation(likes, john, cat);
        
        // Pattern: find all animals
        Node varX = atomSpace.variable("$X");
        Atom pattern = atomSpace.inheritance(varX, animal);
        
        System.out.println("Pattern: " + pattern);
        List<Map<String, Atom>> matches = patternMatcher.findMatches(pattern);
        System.out.println("Matches found: " + matches.size());
        for (Map<String, Atom> match : matches) {
            System.out.println("  $X = " + match.get("$X"));
        }
        
        // Pattern: find what john likes
        Node varY = atomSpace.variable("$Y");
        Atom likesPattern = atomSpace.evaluation(likes, john, varY);
        
        System.out.println("\nPattern: " + likesPattern);
        List<Map<String, Atom>> likesMatches = patternMatcher.findMatches(likesPattern);
        System.out.println("What John likes:");
        for (Map<String, Atom> match : likesMatches) {
            System.out.println("  " + match.get("$Y"));
        }
        
        System.out.println();
    }
    
    /**
     * Demonstrate executable graphs (Atomese)
     */
    private static void demonstrateExecutableGraphs(AtomSpace atomSpace, ExecutionEngine executionEngine) {
        System.out.println("5. EXECUTABLE GRAPHS (ATOMESE)");
        System.out.println("Graphs as code - computation in hypergraph form\n");
        
        // Create arithmetic expressions as graphs
        Atom expr1 = ExecutionEngine.createMathExpression(atomSpace, "3 + 4");
        Atom expr2 = ExecutionEngine.createMathExpression(atomSpace, "2 * 5");
        Atom comparison = ExecutionEngine.createMathExpression(atomSpace, "7 > 10");
        
        atomSpace.add(expr1);
        atomSpace.add(expr2);
        atomSpace.add(comparison);
        
        System.out.println("Mathematical expressions as graphs:");
        System.out.println("  " + expr1);
        System.out.println("  " + expr2);
        System.out.println("  " + comparison);
        
        // Execute the graphs
        System.out.println("\nExecution results:");
        Atom result1 = executionEngine.execute(expr1);
        Atom result2 = executionEngine.execute(expr2);
        Atom result3 = executionEngine.execute(comparison);
        
        System.out.println("  3 + 4 = " + result1);
        System.out.println("  2 * 5 = " + result2);
        System.out.println("  7 > 10 = " + result3 + " (TV: " + result3.getTruthValue() + ")");
        
        // Complex nested expression
        Link complex = new Link(AtomType.PLUS_LINK, expr1, expr2);
        atomSpace.add(complex);
        Atom complexResult = executionEngine.execute(complex);
        
        System.out.println("  (3+4) + (2*5) = " + complexResult);
        
        System.out.println();
    }
    
    /**
     * Demonstrate probabilistic reasoning
     */
    private static void demonstrateProbabilisticReasoning(AtomSpace atomSpace) {
        System.out.println("6. PROBABILISTIC REASONING");
        System.out.println("Truth values with strength and confidence for uncertain reasoning\n");
        
        Node raining = atomSpace.concept("raining");
        Node cloudy = atomSpace.concept("cloudy");
        Node wet_ground = atomSpace.concept("wet-ground");
        
        // Add probabilistic knowledge
        Link rain_implies_wet = atomSpace.inheritance(raining, wet_ground);
        rain_implies_wet.setTruthValue(new TruthValue(0.95, 0.9)); // High strength, high confidence
        
        Link cloudy_implies_rain = atomSpace.inheritance(cloudy, raining);
        cloudy_implies_rain.setTruthValue(new TruthValue(0.3, 0.7)); // Low strength, moderate confidence
        
        // Set evidence
        cloudy.setTruthValue(new TruthValue(0.8, 0.95)); // It's cloudy
        
        System.out.println("Probabilistic knowledge:");
        System.out.println("  Cloudy: " + cloudy.getTruthValue());
        System.out.println("  Cloudy -> Raining: " + cloudy_implies_rain.getTruthValue());
        System.out.println("  Raining -> Wet Ground: " + rain_implies_wet.getTruthValue());
        
        // Simple inference: if it's cloudy, what's the probability of wet ground?
        // This is a simplified version - full PLN would be much more sophisticated
        TruthValue cloudyTv = cloudy.getTruthValue();
        TruthValue toRainTv = cloudy_implies_rain.getTruthValue();
        TruthValue toWetTv = rain_implies_wet.getTruthValue();
        
        // Chain rule approximation
        double prob_rain = cloudyTv.getStrength() * toRainTv.getStrength();
        double prob_wet = prob_rain * toWetTv.getStrength();
        double confidence = Math.min(cloudyTv.getConfidence(), 
                           Math.min(toRainTv.getConfidence(), toWetTv.getConfidence()));
        
        TruthValue inferredWet = new TruthValue(prob_wet, confidence);
        wet_ground.setTruthValue(inferredWet);
        
        System.out.println("\nInferred probability:");
        System.out.println("  Wet Ground: " + wet_ground.getTruthValue());
        
        // Evidence revision
        TruthValue newEvidence = new TruthValue(0.9, 0.8);
        TruthValue revised = wet_ground.getTruthValue().revise(newEvidence);
        System.out.println("  After new evidence: " + revised);
        
        System.out.println();
    }
    
    /**
     * Demonstrate streaming values
     */
    private static void demonstrateStreamingValues(AtomSpace atomSpace) {
        System.out.println("7. STREAMING VALUES");
        System.out.println("Dynamic data flows through static graph structure\n");
        
        Node sensor = atomSpace.concept("temperature-sensor");
        
        // Create streaming temperature data
        StreamValue tempStream = StreamValue.randomFloats(1, 5);
        sensor.setValue("readings", tempStream);
        
        System.out.println("Created streaming temperature sensor");
        System.out.println("  Sensor: " + sensor);
        
        // Sample some values from the stream
        System.out.println("\nStreaming data samples:");
        try {
            for (int i = 0; i < 3; i++) {
                Value reading = tempStream.getNext(100);
                if (reading instanceof FloatValue) {
                    FloatValue fv = (FloatValue) reading;
                    System.out.println("  Reading " + (i+1) + ": " + 
                                     String.format("%.3f", fv.getValue(0)));
                }
            }
        } catch (InterruptedException e) {
            System.out.println("  Stream interrupted");
        }
        
        // Create derived streams
        Node processor = atomSpace.concept("data-processor");
        StreamValue truthStream = StreamValue.varyingTruth(0.6, 0.2, 10);
        processor.setValue("confidence-levels", truthStream);
        
        System.out.println("\nDerived truth value stream:");
        try {
            Value latest = truthStream.getLatest();
            System.out.println("  Latest confidence: " + latest);
        } catch (Exception e) {
            System.out.println("  Stream not ready");
        }
        
        // Clean up streams
        tempStream.stop();
        truthStream.stop();
        
        System.out.println();
    }
    
    /**
     * Demonstrate advanced query capabilities
     */
    private static void demonstrateAdvancedQueries(AtomSpace atomSpace, PatternMatcher patternMatcher) {
        System.out.println("8. ADVANCED QUERIES");
        System.out.println("Sophisticated graph traversal and analysis\n");
        
        Node dog = atomSpace.concept("dog");
        Node animal = atomSpace.concept("animal");
        
        // Find all subclasses of animal
        Set<Atom> subclasses = patternMatcher.getSubclasses(animal);
        System.out.println("Subclasses of 'animal':");
        for (Atom subclass : subclasses) {
            System.out.println("  " + subclass);
        }
        
        // Add similarity relationships
        Node cat = atomSpace.concept("cat");
        Link similarity = atomSpace.similarity(dog, cat);
        similarity.setTruthValue(new TruthValue(0.7, 0.8));
        
        // Find similar animals
        Set<Atom> similar = patternMatcher.getSimilar(dog, 0.5);
        System.out.println("\nAnimals similar to 'dog' (strength > 0.5):");
        for (Atom sim : similar) {
            System.out.println("  " + sim);
        }
        
        // Graph traversal
        Set<Atom> neighborhood = patternMatcher.traverse(dog, 2);
        System.out.println("\nGraph neighborhood of 'dog' (depth 2): " + neighborhood.size() + " atoms");
        
        // Find atoms with high truth values
        Set<Atom> highTruth = atomSpace.getAtomsByTruthValueStrength(0.8);
        System.out.println("Atoms with truth strength > 0.8: " + highTruth.size());
        
        System.out.println();
    }
    
    /**
     * Print comprehensive statistics about the AtomSpace
     */
    private static void printStatistics(AtomSpace atomSpace) {
        System.out.println("9. ATOMSPACE STATISTICS");
        Map<String, Object> stats = atomSpace.getStatistics();
        
        System.out.println("Total atoms: " + stats.get("totalAtoms"));
        System.out.println("Nodes: " + stats.get("nodes"));
        System.out.println("Links: " + stats.get("links"));
        System.out.println("Named nodes: " + stats.get("namedNodes"));
        System.out.println("Types represented: " + stats.get("types"));
        
        @SuppressWarnings("unchecked")
        Map<AtomType, Integer> typeDist = (Map<AtomType, Integer>) stats.get("typeDistribution");
        System.out.println("\nType distribution:");
        typeDist.entrySet().stream()
                .sorted(Map.Entry.<AtomType, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue()));
    }
}