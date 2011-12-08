package org.zwobble.shed.compiler.typechecker.errors;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.types.Type;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class WrongMemberTypeError implements CompilerErrorDescription {
    private final Type superType;
    private final String memberName;
    private final Type expectedType;
    private final Type actualType;
    
    @Override
    public String describe() {
        return "Incorrect implementation of member " + memberName + " in super type " + superType.shortName() + "." + 
            "Expected member of type " + expectedType.shortName() + " but was of type " + actualType;
    }

}

