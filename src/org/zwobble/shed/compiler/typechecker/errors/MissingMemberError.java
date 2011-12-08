package org.zwobble.shed.compiler.typechecker.errors;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.types.Type;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MissingMemberError implements CompilerErrorDescription {
    private final Type superType;
    private final String missingMemberName;
    
    @Override
    public String describe() {
        return "Does not fully implement " + superType.shortName() + ", missing member " + missingMemberName;
    }
}
