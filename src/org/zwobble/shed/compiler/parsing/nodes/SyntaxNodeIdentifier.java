package org.zwobble.shed.compiler.parsing.nodes;

public class SyntaxNodeIdentifier {
    private final SyntaxNode node;

    public SyntaxNodeIdentifier(SyntaxNode node) {
        this.node = node;
    }
    
    public SyntaxNode getNode() {
        return node;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SyntaxNodeIdentifier)) {
            return false;
        }
        return ((SyntaxNodeIdentifier)obj).node == node;
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(node);
    }
}
