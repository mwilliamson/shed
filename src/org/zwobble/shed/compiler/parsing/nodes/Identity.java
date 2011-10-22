package org.zwobble.shed.compiler.parsing.nodes;

import com.google.common.base.Function;

import lombok.ToString;

@ToString
public class Identity<T> {
    public static <T> Function<T, Identity<T>> toIdentity() {
        return new Function<T, Identity<T>>() {
            @Override
            public Identity<T> apply(T input) {
                return new Identity<T>(input);
            }
        };
    }
    
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
