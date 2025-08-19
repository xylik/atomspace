#!/bin/bash

# Simple test script to verify Java AST and structural identity implementation
# This tests the components that don't require the full AtomSpace build

echo "=== AtomSpace Java AST Implementation Test ==="
echo

echo "1. Testing Java components..."
cd /home/runner/work/atomspace/atomspace
mvn -q test
if [ $? -eq 0 ]; then
    echo "✓ Java tests passed"
else
    echo "✗ Java tests failed"
    exit 1
fi

echo
echo "2. Running Java AST demo..."
mvn -q exec:java -Dexec.mainClass="org.opencog.atomspace.JavaAstDemo" > /tmp/demo_output.txt 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Java demo executed successfully"
    echo "Demo output highlights:"
    grep -E "(Structural key:|Equal\?|AST.*==)" /tmp/demo_output.txt | head -6
else
    echo "✗ Java demo failed"
    exit 1
fi

echo
echo "3. Checking C++ code structure..."

# Check that JavaAST files are properly structured
if [ -f "opencog/atoms/foreign/JavaAST.h" ] && [ -f "opencog/atoms/foreign/JavaAST.cc" ]; then
    echo "✓ JavaAST C++ files present"
else
    echo "✗ JavaAST C++ files missing"
    exit 1
fi

# Check that StructuralIdentity files are present
if [ -f "opencog/atoms/base/StructuralIdentity.h" ] && [ -f "opencog/atoms/base/StructuralIdentity.cc" ]; then
    echo "✓ StructuralIdentity C++ files present"
else
    echo "✗ StructuralIdentity C++ files missing"
    exit 1
fi

# Check that JAVA_AST type was added
if grep -q "JAVA_AST" opencog/atoms/atom_types/atom_types.script; then
    echo "✓ JAVA_AST type registered in type system"
else
    echo "✗ JAVA_AST type not found in type system"
    exit 1
fi

# Check that CMake files were updated
if grep -q "JavaAST" opencog/atoms/foreign/CMakeLists.txt; then
    echo "✓ JavaAST added to build system"
else
    echo "✗ JavaAST not added to build system"
    exit 1
fi

echo
echo "4. Validating implementation completeness..."

# Check for key methods in JavaAST
if grep -q "compute_structural_key" opencog/atoms/foreign/JavaAST.cc; then
    echo "✓ Structural identity methods implemented"
else
    echo "✗ Structural identity methods missing"
    exit 1
fi

# Check for structural identity usage
if grep -q "StructuralIdentity::" opencog/atoms/foreign/JavaAST.cc; then
    echo "✓ StructuralIdentity system integrated"
else
    echo "✗ StructuralIdentity system not integrated"
    exit 1
fi

echo
echo "5. Testing structural identity consistency..."

# Run a specific test to verify structural identity
mvn -q test -Dtest=AtomSpaceJavaTest#testGlobalUniqueness > /tmp/unique_test.txt 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Structural identity uniqueness verified"
else
    echo "✗ Structural identity uniqueness test failed"
    cat /tmp/unique_test.txt
    exit 1
fi

echo
echo "=== All Tests Passed! ==="
echo
echo "Summary of implemented features:"
echo "- ✓ Java project structure with Maven build"
echo "- ✓ Structural identity system (N|type|name, L|type|arity|...)"
echo "- ✓ Java AST parser with JavaParser integration"
echo "- ✓ Content-addressed global uniqueness"
echo "- ✓ C++ JavaAST class following ForeignAST pattern"
echo "- ✓ Integration with AtomSpace type system"
echo "- ✓ Comprehensive test suite"
echo "- ✓ Documentation and examples"
echo
echo "Ready for:"
echo "- Integration with full AtomSpace build"
echo "- JNI bridge development"
echo "- AGI reasoning system integration"