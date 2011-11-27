package org.zwobble.shed.compiler.typechecker.errors;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.types.Type;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class NotAnInterfaceError implements CompilerErrorDescription {
    private final Type type;
    
    @Override
    public String describe() {
        return type.shortName() + " is not an interface";
    }
}
