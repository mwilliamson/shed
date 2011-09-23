package org.zwobble.shed.compiler.parsing.nodes.structure;

import java.util.Iterator;

import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

public class ScopedNodes implements Iterable<SyntaxNode> {
    public static enum Scope {
        SAME,
        SUB_SCOPE,
        EXTENDED_SCOPE
    }
    
    public static ScopedNodes sameScope(Iterable<? extends SyntaxNode> nodes) {
        return new ScopedNodes(nodes, Scope.SAME);
    }
    
    public static ScopedNodes subScope(Iterable<? extends SyntaxNode> nodes) {
        return new ScopedNodes(nodes, Scope.SUB_SCOPE);
    }
    
    public static ScopedNodes extendedScope(Iterable<? extends SyntaxNode> nodes) {
        return new ScopedNodes(nodes, Scope.EXTENDED_SCOPE);
    }
    
    private final Iterable<? extends SyntaxNode> nodes;
    private final Scope scope;
    
    private ScopedNodes(Iterable<? extends SyntaxNode> nodes, Scope scope) {
        this.nodes = nodes;
        this.scope = scope;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<SyntaxNode> iterator() {
        return (Iterator<SyntaxNode>)nodes.iterator();
    }
    
    public Scope getScope() {
        return scope;
    }
}
