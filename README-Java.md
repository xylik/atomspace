# AtomSpace Java Integration

This directory contains the Java AST parser and AtomSpace integration components that implement the comprehensive Java POC for AGI development.

## Overview

This implementation provides:

1. **Structural Identity System**: Content-addressed global uniqueness using canonical string keys
2. **Java AST Parsing**: Conversion of Java source code into AtomSpace-compatible structures
3. **AtomSpace Integration**: Ready for JNI bridge to C++ AtomSpace core

## Key Features

### Structural Identity

The system replaces hashCode-based interning with canonical structural key strings:

- **Node keys**: `N|<type>|<name>` (e.g., `N|ConceptNode|hello`)
- **Link keys**: `L|<type>|<arity>|<child1_key>|<child2_key>|...` (e.g., `L|ListLink|2|N|ConceptNode|A|N|ConceptNode|B`)

This provides:
- Content-addressed identity
- Global uniqueness across different systems
- Deterministic structure representation

### Java AST Parsing

The JavaAstParser converts Java source code into structured AtomSpace representations:

```java
JavaAstParser parser = new JavaAstParser();
Atom ast = parser.parseJavaCode(javaSource);
String atomese = ast.toAtomese(); // Convert to Atomese format
String key = ast.getStructuralKey(); // Get canonical identity key
```

Supported Java constructs:
- Class declarations
- Method declarations
- Variable declarations
- Binary expressions
- Method calls
- Control flow (if/else, loops)
- Literals (string, integer, etc.)

### Core Classes

- **`Atom`**: Abstract base class for all AtomSpace elements
- **`Node`**: Atomic (leaf) elements with type and name
- **`Link`**: Composite elements containing other atoms
- **`JavaAstParser`**: Converts Java source to AtomSpace structures

## Building and Testing

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Run the demo
mvn exec:java -Dexec.mainClass="org.opencog.atomspace.JavaAstDemo"
```

## Example Usage

```java
// Create nodes with structural identity
Node concept = new Node("ConceptNode", "artificial_intelligence");
System.out.println(concept.getStructuralKey()); // N|ConceptNode|artificial_intelligence

// Create links
Link relationship = new Link("EvaluationLink", List.of(
    new Node("PredicateNode", "is_type_of"),
    concept,
    new Node("ConceptNode", "technology")
));

// Parse Java code
JavaAstParser parser = new JavaAstParser();
Atom ast = parser.parseJavaCode("""
    public class AIAgent {
        public void think() {
            // AGI thinking process
        }
    }
""");

// Convert to Atomese for AtomSpace storage
String atomese = ast.toAtomese();
```

## Integration with C++ AtomSpace

The Java components are designed to integrate with the C++ AtomSpace core via:

1. **JavaAST C++ class**: Handles Java AST atoms in the C++ core
2. **JNI Bridge**: Enables communication between Java and C++ components
3. **Structural Identity**: Consistent identity system across languages

The C++ JavaAST class is located in `opencog/atoms/foreign/JavaAST.{h,cc}` and registered as the `JAVA_AST` atom type.

## Architecture

```
Java Layer:
├── JavaAstParser     -> Parse Java code into structured atoms
├── Atom/Node/Link    -> Core structural identity system
└── Demo/Tests        -> Demonstrations and validation

C++ Layer:
├── JavaAST           -> Handle Java AST atoms in AtomSpace
├── ForeignAST        -> Base class for foreign language ASTs
└── AtomSpace Core    -> Storage and querying engine

Integration:
└── JNI Bridge        -> Communication between Java and C++
```

## Benefits for AGI Development

1. **Knowledge Representation**: Java code becomes queryable knowledge
2. **Code Understanding**: Structural analysis of software systems
3. **Cross-Language Integration**: Unified representation across languages
4. **AGI Reasoning**: Apply AtomSpace reasoning to software artifacts
5. **Self-Modifying Systems**: AI systems can analyze and modify their own code

## Next Steps

1. Implement JNI bridge for Java-C++ communication
2. Add support for more Java language constructs
3. Integrate with AtomSpace pattern matcher for code querying
4. Develop AGI reasoning modules for software understanding
5. Create tools for software analysis and transformation

This implementation provides a solid foundation for using Java as both a development language and a knowledge representation format within AGI systems.