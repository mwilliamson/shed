package org.zwobble.shed.compiler.typechecker.errors;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.types.Type;

import lombok.Data;

@Data
public class WrongReturnTypeError implements CompilerErrorDescription {
    private final Type expectedType;
    private final Type actualType;
    
    @Override
    public String describe() {
        return "Expected return expression of type \"" + expectedType.shortName() + "\" but was of type \"" + actualType.shortName() + "\"";
    }
}
