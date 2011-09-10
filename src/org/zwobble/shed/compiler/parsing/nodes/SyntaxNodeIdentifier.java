package org.zwobble.shed.compiler.parsing.nodes;

import lombok.ToString;

@ToString
public class SyntaxNodeIdentifier {
    private final Node node;

    public SyntaxNodeIdentifier(Node node) {
        this.node = node;
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
