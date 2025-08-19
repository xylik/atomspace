package org.opencog.atomspace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests for the structural identity system and Java AST parsing.
 */
public class AtomSpaceJavaTest {
    
    private JavaAstParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new JavaAstParser();
    }
    
    @Test
    void testNodeStructuralIdentity() {
        // Test that structural identity works for Nodes
        Node node1 = new Node("ConceptNode", "test");
        Node node2 = new Node("ConceptNode", "test");
        Node node3 = new Node("ConceptNode", "different");
        
        assertEquals(node1, node2, "Nodes with same type and name should be equal");
        assertNotEquals(node1, node3, "Nodes with different names should not be equal");
        assertEquals(node1.getStructuralKey(), node2.getStructuralKey());
        assertNotEquals(node1.getStructuralKey(), node3.getStructuralKey());
    }
    
    @Test
    void testLinkStructuralIdentity() {
        // Test that structural identity works for Links
        Node nodeA = new Node("ConceptNode", "A");
        Node nodeB = new Node("ConceptNode", "B");
        
        Link link1 = new Link("ListLink", List.of(nodeA, nodeB));
        Link link2 = new Link("ListLink", List.of(nodeA, nodeB));
        Link link3 = new Link("ListLink", List.of(nodeB, nodeA));
        
        assertEquals(link1, link2, "Links with same structure should be equal");
        assertNotEquals(link1, link3, "Links with different order should not be equal");
        assertEquals(link1.getStructuralKey(), link2.getStructuralKey());
        assertNotEquals(link1.getStructuralKey(), link3.getStructuralKey());
    }
    
    @Test
    void testStructuralKeyFormat() {
        // Test the format of structural keys
        Node node = new Node("ConceptNode", "test");
        assertEquals("N|ConceptNode|test", node.getStructuralKey());
        
        Link link = new Link("ListLink", List.of(node));
        assertEquals("L|ListLink|1|N|ConceptNode|test", link.getStructuralKey());
    }
    
    @Test
    void testAtomeseGeneration() {
        // Test conversion to Atomese format
        Node node = new Node("ConceptNode", "hello");
        assertEquals("(ConceptNode \"hello\")", node.toAtomese());
        
        Link link = new Link("ListLink", List.of(node));
        assertEquals("(ListLink (ConceptNode \"hello\"))", link.toAtomese());
    }
    
    @Test
    void testJavaAstParsing() {
        // Test basic Java AST parsing
        String javaCode = """
            package com.example;
            
            public class Hello {
                public String greet(String name) {
                    return "Hello, " + name;
                }
            }
            """;
        
        Atom ast = parser.parseJavaCode(javaCode);
        
        assertNotNull(ast);
        assertTrue(ast instanceof Link);
        assertEquals("CompilationUnit", ast.getType());
        
        Link compilationUnit = (Link) ast;
        assertTrue(compilationUnit.getArity() > 0, "Should have parsed some content");
        
        // Check that we can convert back to Atomese
        String atomese = ast.toAtomese();
        assertNotNull(atomese);
        assertTrue(atomese.startsWith("(CompilationUnit"));
    }
    
    @Test 
    void testJavaAstStructuralKeys() {
        // Test that Java AST produces consistent structural keys
        String javaCode = "public class Test { }";
        
        Atom ast1 = parser.parseJavaCode(javaCode);
        Atom ast2 = parser.parseJavaCode(javaCode);
        
        assertEquals(ast1.getStructuralKey(), ast2.getStructuralKey(),
            "Same Java code should produce same structural key");
    }
    
    @Test
    void testGlobalUniqueness() {
        // Test that the structural identity system provides global uniqueness
        Node node1 = new Node("ConceptNode", "global_test");
        Node node2 = new Node("ConceptNode", "global_test");
        
        // These should be considered identical even across different instances
        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals(node1, node2);
        
        // The structural key should be deterministic and globally unique
        String key = node1.getStructuralKey();
        assertTrue(key.contains("ConceptNode"));
        assertTrue(key.contains("global_test"));
        assertEquals(key, node2.getStructuralKey());
    }
}