package org.opencog.atomspace;

/**
 * Demonstration of Java AST -> AtomSpace import capability.
 * This shows how Java code can be parsed into structural representations
 * suitable for AtomSpace storage and querying.
 */
public class JavaAstDemo {
    
    public static void main(String[] args) {
        System.out.println("=== AtomSpace Java AST Demo ===\n");
        
        // Create parser
        JavaAstParser parser = new JavaAstParser();
        
        // Demo 1: Simple structural identity
        System.out.println("1. Structural Identity Demo:");
        Node node1 = new Node("ConceptNode", "hello");
        Node node2 = new Node("ConceptNode", "hello");
        System.out.println("Node 1: " + node1.toAtomese());
        System.out.println("Node 2: " + node2.toAtomese());
        System.out.println("Structural key: " + node1.getStructuralKey());
        System.out.println("Equal? " + node1.equals(node2));
        System.out.println();
        
        // Demo 2: Link structures
        System.out.println("2. Link Structure Demo:");
        Link link = new Link("ListLink", java.util.List.of(
            new Node("ConceptNode", "A"),
            new Node("ConceptNode", "B")
        ));
        System.out.println("Link: " + link.toAtomese());
        System.out.println("Structural key: " + link.getStructuralKey());
        System.out.println("Arity: " + link.getArity());
        System.out.println();
        
        // Demo 3: Java AST parsing
        System.out.println("3. Java AST Parsing Demo:");
        String javaCode = """
            public class Greeting {
                private String name;
                
                public String greet() {
                    return "Hello, " + name + "!";
                }
            }
            """;
        
        System.out.println("Java Code:");
        System.out.println(javaCode);
        System.out.println();
        
        Atom ast = parser.parseJavaCode(javaCode);
        System.out.println("Parsed AST:");
        System.out.println("Type: " + ast.getType());
        System.out.println("Structural Key: " + ast.getStructuralKey());
        System.out.println();
        System.out.println("AtomSpace Representation:");
        System.out.println(ast.toAtomese());
        System.out.println();
        
        // Demo 4: Complex expression parsing
        System.out.println("4. Expression Parsing Demo:");
        String methodCode = """
            public int calculate(int x, int y) {
                if (x > y) {
                    return x * 2 + y;
                } else {
                    return y * 2 + x;
                }
            }
            """;
        
        Atom methodAst = parser.parseJavaCode(methodCode);
        System.out.println("Method AST:");
        System.out.println(methodAst.toAtomese());
        System.out.println();
        
        // Demo 5: Content-addressed uniqueness
        System.out.println("5. Content-Addressed Uniqueness Demo:");
        Atom ast1 = parser.parseJavaCode("public class Test { }");
        Atom ast2 = parser.parseJavaCode("public class Test { }");
        Atom ast3 = parser.parseJavaCode("public class Different { }");
        
        System.out.println("AST 1 key: " + ast1.getStructuralKey());
        System.out.println("AST 2 key: " + ast2.getStructuralKey());
        System.out.println("AST 3 key: " + ast3.getStructuralKey());
        System.out.println("AST 1 == AST 2? " + ast1.equals(ast2));
        System.out.println("AST 1 == AST 3? " + ast1.equals(ast3));
        
        System.out.println("\n=== Demo Complete ===");
        System.out.println("This demonstrates:");
        System.out.println("- Structural identity with canonical keys");
        System.out.println("- Java AST parsing into AtomSpace structures");
        System.out.println("- Content-addressed global uniqueness");
        System.out.println("- Ready for integration with C++ AtomSpace via JNI");
    }
}