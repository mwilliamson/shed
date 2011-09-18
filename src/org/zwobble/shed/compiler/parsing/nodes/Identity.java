package org.zwobble.shed.compiler.parsing.nodes;

import lombok.ToString;

@ToString
public class Identity<T extends Node> {
    private final T node;

    public Identity(T node) {
        this.node = node;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Identity)) {
            return false;
        }
        return ((Identity<T>)obj).node == node;
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(node);
    }
    
    public T get() {
        return node;
    }
}
