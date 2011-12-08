package org.zwobble.shed.compiler.typechecker.errors;

import lombok.Data;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.types.Type;

@Data
public class TypeMismatchError implements CompilerErrorDescription {
    private final Type expectedType;
    private final Type actualType;
    
    @Override
    public String describe() {
        return "Expected expression of type " + expectedType.shortName() + " but was of type " + actualType.shortName();
    }
    
    @Override
    public String toString() {
        return describe();
    }
}
