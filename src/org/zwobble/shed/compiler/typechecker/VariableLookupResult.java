package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.types.Type;

import lombok.Data;

@Data
public class VariableLookupResult {
    public static VariableLookupResult success(ValueInfo type) {
        return new VariableLookupResult(Status.SUCCESS, type);
    }
    
    public static VariableLookupResult notDeclared() {
        return new VariableLookupResult(Status.NOT_DECLARED, null);
    }
    
    private final Status status;
    private final ValueInfo valueInfo;
    
    public Type getType() {
        return valueInfo.getType();
    }
    
    public Option<ShedValue> getValue() {
        return valueInfo.getValue();
    }
    
    public enum Status {
        SUCCESS,
        NOT_DECLARED
    }
}
