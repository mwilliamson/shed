package org.zwobble.shed.compiler.typechecker.errors;

import lombok.Data;

import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.types.Type;

@Data
public class NotCallableError implements CompilerErrorDescription {
    private final Type type;
    
    @Override
    public String describe() {
        return "Cannot call expressions of type " + type.shortName();
    }
}
