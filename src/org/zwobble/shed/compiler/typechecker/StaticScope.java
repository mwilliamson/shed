package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.Type;

public class StaticScope {
    private final Map<String, Type> declaredValues = new HashMap<String, Type>();
    private final Set<String> undeclaredValues = new HashSet<String>();
    private final Option<Type> returnType;
    
    public StaticScope(Option<Type> returnType) {
        this.returnType = returnType;
    }
    
    public void add(String identifier, Type type) {
        if (isDeclared(identifier)) {
            throw new RuntimeException("Identifier is already declared: " + identifier);
        }
        declaredValues.put(identifier, type);
    }
    
    public boolean isDeclared(String identifier) {
        return declaredValues.containsKey(identifier);
    }
    
    public VariableLookupResult get(String identifier) {
        if (declaredValues.containsKey(identifier)) {
            return new VariableLookupResult(Status.SUCCESS, declaredValues.get(identifier));            
        }
        if (undeclaredValues.contains(identifier)) {
            return new VariableLookupResult(Status.NOT_DECLARED_YET, null);
        }
        return new VariableLookupResult(Status.NOT_DECLARED, null);
    }
    
    public Option<Type> getReturnType() {
        return returnType;
    }

    public void declaredSoon(String identifier) {
        undeclaredValues.add(identifier);
    }
}
