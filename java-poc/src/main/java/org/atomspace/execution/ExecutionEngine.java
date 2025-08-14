package org.atomspace.execution;

import org.atomspace.core.*;
import org.atomspace.types.AtomType;
import org.atomspace.values.*;

import java.util.*;

/**
 * ExecutionEngine implements executable graphs (Atomese).
 * 
 * One of AtomSpace's key innovations is that graphs themselves can be executable,
 * similar to abstract syntax trees (ASTs) in compilers. This enables:
 * 
 * 1. Procedural knowledge representation
 * 2. Rule-based reasoning systems  
 * 3. Mathematical computations in graph form
 * 4. Dynamic behavior within knowledge graphs
 * 
 * The graphs encode computation while Values flow through them as data.
 */
public class ExecutionEngine {
    
    private final AtomSpace atomSpace;
    
    public ExecutionEngine(AtomSpace atomSpace) {
        this.atomSpace = atomSpace;
    }
    
    /**
     * Execute an atom and return its result.
     * 
     * @param atom The atom to execute
     * @return The result of execution, or the atom itself if not executable
     */
    public Atom execute(Atom atom) {
        if (atom instanceof Node) {
            return executeNode((Node) atom);
        } else if (atom instanceof Link) {
            return executeLink((Link) atom);
        }
        return atom;
    }
    
    /**
     * Execute a Node - usually just returns the node itself
     */
    private Atom executeNode(Node node) {
        // Most nodes are just data, but some special ones could have behavior
        switch (node.getType()) {
            case SCHEMA_NODE:
                // Schema nodes could represent procedures
                return executeSchema(node);
            default:
                return node;
        }
    }
    
    /**
     * Execute a Schema node (procedure)
     */
    private Atom executeSchema(Node schemaNode) {
        // For this POC, we'll just return the schema itself
        // In a full implementation, this would look up and execute the schema's definition
        return schemaNode;
    }
    
    /**
     * Execute a Link based on its type
     */
    private Atom executeLink(Link link) {
        switch (link.getType()) {
            case PLUS_LINK:
                return executePlus(link);
            case TIMES_LINK:
                return executeTimes(link);
            case GREATER_THAN_LINK:
                return executeGreaterThan(link);
            case AND_LINK:
                return executeAnd(link);
            case OR_LINK:
                return executeOr(link);
            case NOT_LINK:
                return executeNot(link);
            case EXECUTION_OUTPUT_LINK:
                return executeExecutionOutput(link);
            case LIST_LINK:
                return executeList(link);
            default:
                return link; // Non-executable link
        }
    }
    
    /**
     * Execute arithmetic addition
     */
    private Atom executePlus(Link plusLink) {
        double sum = 0.0;
        
        for (Atom arg : plusLink.getOutgoing()) {
            Atom executed = execute(arg);
            double value = extractNumericValue(executed);
            sum += value;
        }
        
        // Create a concept node with the result
        Node result = atomSpace.concept(String.valueOf(sum));
        result.setValue("numeric-value", new FloatValue(sum));
        return result;
    }
    
    /**
     * Execute arithmetic multiplication
     */
    private Atom executeTimes(Link timesLink) {
        double product = 1.0;
        
        for (Atom arg : timesLink.getOutgoing()) {
            Atom executed = execute(arg);
            double value = extractNumericValue(executed);
            product *= value;
        }
        
        Node result = atomSpace.concept(String.valueOf(product));
        result.setValue("numeric-value", new FloatValue(product));
        return result;
    }
    
    /**
     * Execute greater-than comparison
     */
    private Atom executeGreaterThan(Link gtLink) {
        if (gtLink.getArity() != 2) {
            throw new IllegalArgumentException("GreaterThanLink must have exactly 2 arguments");
        }
        
        Atom left = execute(gtLink.getOutgoing(0));
        Atom right = execute(gtLink.getOutgoing(1));
        
        double leftVal = extractNumericValue(left);
        double rightVal = extractNumericValue(right);
        
        boolean result = leftVal > rightVal;
        Node boolResult = atomSpace.concept(String.valueOf(result));
        boolResult.setTruthValue(result ? TruthValue.TRUE : TruthValue.FALSE);
        
        return boolResult;
    }
    
    /**
     * Execute logical AND
     */
    private Atom executeAnd(Link andLink) {
        for (Atom arg : andLink.getOutgoing()) {
            Atom executed = execute(arg);
            if (!isTrueAtom(executed)) {
                Node result = atomSpace.concept("false");
                result.setTruthValue(TruthValue.FALSE);
                return result;
            }
        }
        
        Node result = atomSpace.concept("true");
        result.setTruthValue(TruthValue.TRUE);
        return result;
    }
    
    /**
     * Execute logical OR
     */
    private Atom executeOr(Link orLink) {
        for (Atom arg : orLink.getOutgoing()) {
            Atom executed = execute(arg);
            if (isTrueAtom(executed)) {
                Node result = atomSpace.concept("true");
                result.setTruthValue(TruthValue.TRUE);
                return result;
            }
        }
        
        Node result = atomSpace.concept("false");
        result.setTruthValue(TruthValue.FALSE);
        return result;
    }
    
    /**
     * Execute logical NOT
     */
    private Atom executeNot(Link notLink) {
        if (notLink.getArity() != 1) {
            throw new IllegalArgumentException("NotLink must have exactly 1 argument");
        }
        
        Atom arg = execute(notLink.getOutgoing(0));
        boolean isTrue = isTrueAtom(arg);
        
        Node result = atomSpace.concept(String.valueOf(!isTrue));
        result.setTruthValue(isTrue ? TruthValue.FALSE : TruthValue.TRUE);
        return result;
    }
    
    /**
     * Execute an ExecutionOutputLink - runs a procedure with arguments
     */
    private Atom executeExecutionOutput(Link execLink) {
        if (execLink.getArity() < 1) {
            throw new IllegalArgumentException("ExecutionOutputLink must have at least 1 argument");
        }
        
        Atom procedure = execLink.getOutgoing(0);
        List<Atom> args = execLink.getOutgoing().subList(1, execLink.getArity());
        
        // For this POC, we'll handle some built-in procedures
        if (procedure instanceof Node) {
            Node procNode = (Node) procedure;
            return executeBuiltinProcedure(procNode.getName(), args);
        }
        
        return execLink; // Unknown procedure
    }
    
    /**
     * Execute a list - just execute all elements
     */
    private Atom executeList(Link listLink) {
        List<Atom> executedElements = new ArrayList<>();
        for (Atom element : listLink.getOutgoing()) {
            executedElements.add(execute(element));
        }
        return atomSpace.list(executedElements.toArray(new Atom[0]));
    }
    
    /**
     * Execute built-in procedures
     */
    private Atom executeBuiltinProcedure(String procName, List<Atom> args) {
        switch (procName.toLowerCase()) {
            case "random":
                return generateRandomNumber(args);
            case "max":
                return findMaximum(args);
            case "min":
                return findMinimum(args);
            case "count":
                return countArguments(args);
            default:
                // Unknown procedure - could look it up in atomspace
                Node result = atomSpace.concept("unknown-procedure");
                result.setValue("procedure-name", new StringValue(procName));
                return result;
        }
    }
    
    private Atom generateRandomNumber(List<Atom> args) {
        Random random = new Random();
        double value = random.nextDouble();
        
        Node result = atomSpace.concept(String.valueOf(value));
        result.setValue("numeric-value", new FloatValue(value));
        return result;
    }
    
    private Atom findMaximum(List<Atom> args) {
        double max = Double.NEGATIVE_INFINITY;
        for (Atom arg : args) {
            Atom executed = execute(arg);
            double value = extractNumericValue(executed);
            max = Math.max(max, value);
        }
        
        Node result = atomSpace.concept(String.valueOf(max));
        result.setValue("numeric-value", new FloatValue(max));
        return result;
    }
    
    private Atom findMinimum(List<Atom> args) {
        double min = Double.POSITIVE_INFINITY;
        for (Atom arg : args) {
            Atom executed = execute(arg);
            double value = extractNumericValue(executed);
            min = Math.min(min, value);
        }
        
        Node result = atomSpace.concept(String.valueOf(min));
        result.setValue("numeric-value", new FloatValue(min));
        return result;
    }
    
    private Atom countArguments(List<Atom> args) {
        double count = args.size();
        Node result = atomSpace.concept(String.valueOf(count));
        result.setValue("numeric-value", new FloatValue(count));
        return result;
    }
    
    /**
     * Extract numeric value from an atom
     */
    private double extractNumericValue(Atom atom) {
        // Try to get from FloatValue first
        Value numValue = atom.getValue("numeric-value");
        if (numValue instanceof FloatValue) {
            FloatValue fv = (FloatValue) numValue;
            return fv.size() > 0 ? fv.getValue(0) : 0.0;
        }
        
        // Try to parse from node name
        if (atom instanceof Node) {
            Node node = (Node) atom;
            try {
                return Double.parseDouble(node.getName());
            } catch (NumberFormatException e) {
                // Not a number, return 0
                return 0.0;
            }
        }
        
        return 0.0;
    }
    
    /**
     * Check if an atom represents a true value
     */
    private boolean isTrueAtom(Atom atom) {
        TruthValue tv = atom.getTruthValue();
        if (tv != null && tv.getConfidence() > 0.5) {
            return tv.getStrength() > 0.5;
        }
        
        // Check for explicit true/false names
        if (atom instanceof Node) {
            Node node = (Node) atom;
            String name = node.getName().toLowerCase();
            if ("true".equals(name) || "1".equals(name)) return true;
            if ("false".equals(name) || "0".equals(name)) return false;
        }
        
        // Default to false for unknown
        return false;
    }
    
    /**
     * Create an executable expression for testing
     */
    public static Atom createMathExpression(AtomSpace atomSpace, String expression) {
        // Simple expression parser for demo purposes
        // This would be much more sophisticated in a real implementation
        
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            List<Atom> args = new ArrayList<>();
            for (String part : parts) {
                args.add(createNumericNode(atomSpace, part.trim()));
            }
            return new Link(AtomType.PLUS_LINK, args);
        } else if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            List<Atom> args = new ArrayList<>();
            for (String part : parts) {
                args.add(createNumericNode(atomSpace, part.trim()));
            }
            return new Link(AtomType.TIMES_LINK, args);
        } else if (expression.contains(">")) {
            String[] parts = expression.split(">");
            if (parts.length == 2) {
                Atom left = createNumericNode(atomSpace, parts[0].trim());
                Atom right = createNumericNode(atomSpace, parts[1].trim());
                return new Link(AtomType.GREATER_THAN_LINK, left, right);
            }
        }
        
        // Single number
        return createNumericNode(atomSpace, expression);
    }
    
    private static Node createNumericNode(AtomSpace atomSpace, String value) {
        Node node = atomSpace.concept(value);
        try {
            double numValue = Double.parseDouble(value);
            node.setValue("numeric-value", new FloatValue(numValue));
        } catch (NumberFormatException e) {
            // Not a number, just store as concept
        }
        return node;
    }
}