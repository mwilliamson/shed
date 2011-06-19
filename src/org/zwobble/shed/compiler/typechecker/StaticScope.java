package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.types.Type;

public class StaticScope {
    private final Map<String, Type> values = new HashMap<String, Type>();
    
    public void add(String identifier, Type type) {
        if (isDeclared(identifier)) {
            throw new RuntimeException("Identifier is already declared: " + identifier);
        }
        values.put(identifier, type);
    }
    
    public boolean isDeclared(String identifier) {
        return values.containsKey(identifier);
    }
    
    public Type get(String identifier) {
        if (!isDeclared(identifier)) {
            throw new RuntimeException("Identifier is not declared: " + identifier);
        }
        return values.get(identifier);
    }
}
