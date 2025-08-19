package org.opencog.atomspace;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser that converts Java AST into AtomSpace Atom structures.
 * This implements the Java AST -> Atom import capability.
 */
public class JavaAstParser {
    private final JavaParser parser;
    
    public JavaAstParser() {
        this.parser = new JavaParser();
    }
    
    /**
     * Parse Java source code and convert to AtomSpace representation.
     */
    public Atom parseJavaCode(String javaSource) {
        try {
            CompilationUnit cu = parser.parse(javaSource).getResult().orElseThrow();
            return convertCompilationUnit(cu);
        } catch (Exception e) {
            // Fallback: create a simple JavaAST node with the raw source
            return new Node("JavaAST", javaSource);
        }
    }
    
    private Atom convertCompilationUnit(CompilationUnit cu) {
        List<Atom> children = new ArrayList<>();
        
        // Convert package declaration if present
        cu.getPackageDeclaration().ifPresent(pkg -> 
            children.add(new Node("PackageDeclaration", pkg.getNameAsString())));
        
        // Convert imports
        cu.getImports().forEach(imp -> 
            children.add(new Node("ImportDeclaration", imp.getNameAsString())));
        
        // Convert types (classes, interfaces, etc.)
        cu.getTypes().forEach(type -> children.add(convertTypeDeclaration(type)));
        
        return new Link("CompilationUnit", children);
    }
    
    private Atom convertTypeDeclaration(com.github.javaparser.ast.body.TypeDeclaration<?> type) {
        List<Atom> children = new ArrayList<>();
        
        // Add type name
        children.add(new Node("TypeName", type.getNameAsString()));
        
        if (type instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration classDecl = (ClassOrInterfaceDeclaration) type;
            
            // Add modifiers
            classDecl.getModifiers().forEach(mod -> 
                children.add(new Node("Modifier", mod.getKeyword().asString())));
            
            // Add extends clause
            classDecl.getExtendedTypes().forEach(ext -> 
                children.add(new Link("Extends", List.of(new Node("TypeName", ext.getNameAsString())))));
            
            // Add implements clause  
            classDecl.getImplementedTypes().forEach(impl -> 
                children.add(new Link("Implements", List.of(new Node("TypeName", impl.getNameAsString())))));
            
            // Add methods
            classDecl.getMethods().forEach(method -> children.add(convertMethod(method)));
            
            return new Link("ClassDeclaration", children);
        }
        
        return new Link("TypeDeclaration", children);
    }
    
    private Atom convertMethod(MethodDeclaration method) {
        List<Atom> children = new ArrayList<>();
        
        // Method name
        children.add(new Node("MethodName", method.getNameAsString()));
        
        // Return type
        children.add(new Node("ReturnType", method.getType().asString()));
        
        // Parameters
        method.getParameters().forEach(param -> {
            List<Atom> paramChildren = List.of(
                new Node("ParameterType", param.getType().asString()),
                new Node("ParameterName", param.getNameAsString())
            );
            children.add(new Link("Parameter", paramChildren));
        });
        
        // Method body
        method.getBody().ifPresent(body -> 
            children.add(convertBlockStatement(body)));
        
        return new Link("MethodDeclaration", children);
    }
    
    private Atom convertBlockStatement(BlockStmt block) {
        List<Atom> children = new ArrayList<>();
        
        block.getStatements().forEach(stmt -> children.add(convertStatement(stmt)));
        
        return new Link("BlockStatement", children);
    }
    
    private Atom convertStatement(Statement stmt) {
        if (stmt instanceof ExpressionStmt) {
            return convertExpression(((ExpressionStmt) stmt).getExpression());
        } else if (stmt instanceof ReturnStmt) {
            ReturnStmt returnStmt = (ReturnStmt) stmt;
            List<Atom> children = new ArrayList<>();
            returnStmt.getExpression().ifPresent(expr -> children.add(convertExpression(expr)));
            return new Link("ReturnStatement", children);
        } else if (stmt instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) stmt;
            List<Atom> children = List.of(
                convertExpression(ifStmt.getCondition()),
                convertStatement(ifStmt.getThenStmt())
            );
            if (ifStmt.getElseStmt().isPresent()) {
                children = new ArrayList<>(children);
                children.add(convertStatement(ifStmt.getElseStmt().get()));
            }
            return new Link("IfStatement", children);
        }
        
        // Fallback for other statement types
        return new Node("Statement", stmt.toString());
    }
    
    private Atom convertExpression(Expression expr) {
        if (expr instanceof NameExpr) {
            return new Node("Variable", ((NameExpr) expr).getNameAsString());
        } else if (expr instanceof StringLiteralExpr) {
            return new Node("StringLiteral", ((StringLiteralExpr) expr).getValue());
        } else if (expr instanceof IntegerLiteralExpr) {
            return new Node("IntegerLiteral", ((IntegerLiteralExpr) expr).getValue());
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binExpr = (BinaryExpr) expr;
            return new Link("BinaryExpression", List.of(
                convertExpression(binExpr.getLeft()),
                new Node("Operator", binExpr.getOperator().asString()),
                convertExpression(binExpr.getRight())
            ));
        } else if (expr instanceof MethodCallExpr) {
            MethodCallExpr methodCall = (MethodCallExpr) expr;
            List<Atom> children = new ArrayList<>();
            
            methodCall.getScope().ifPresent(scope -> children.add(convertExpression(scope)));
            children.add(new Node("MethodName", methodCall.getNameAsString()));
            
            methodCall.getArguments().forEach(arg -> children.add(convertExpression(arg)));
            
            return new Link("MethodCall", children);
        }
        
        // Fallback for other expression types
        return new Node("Expression", expr.toString());
    }
}