package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.types.Type;

import lombok.Data;

@Data
public class VariableLookupResult {
    public static VariableLookupResult success(Type type) {
        return new VariableLookupResult(Status.SUCCESS, type);
    }
    
    public static VariableLookupResult notDeclared() {
        return new VariableLookupResult(Status.NOT_DECLARED, null);
    }
    
    public static VariableLookupResult notDeclaredYet() {
        return new VariableLookupResult(Status.NOT_DECLARED_YET, null);
    }
    
    private final Status status;
    private final Type type;
    
    public enum Status {
        SUCCESS,
        NOT_DECLARED,
        NOT_DECLARED_YET
    }
}
