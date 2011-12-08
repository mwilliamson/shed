package org.zwobble.shed.compiler.typechecker.errors;

import lombok.ToString;

import lombok.EqualsAndHashCode;

import lombok.AllArgsConstructor;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.types.Type;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class NotScalarTypeError implements CompilerErrorDescription {
    private final Type actualType;
    
    @Override
    public String describe() {
        return "Expected left-hand-side to be scalar type, but was of type " + actualType.shortName();
    }
    
}
