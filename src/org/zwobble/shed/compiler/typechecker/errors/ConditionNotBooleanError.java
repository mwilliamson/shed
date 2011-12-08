package org.zwobble.shed.compiler.typechecker.errors;

import lombok.Data;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

@Data
public class ConditionNotBooleanError implements CompilerErrorDescription {
    private final Type actualType;
    
    @Override
    public String describe() {
        return "Condition must be of type " + CoreTypes.BOOLEAN.shortName() + ", was of type " + actualType.shortName();
    }
}
