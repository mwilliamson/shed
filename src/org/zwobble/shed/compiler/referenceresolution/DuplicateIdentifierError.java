package org.zwobble.shed.compiler.referenceresolution;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.CompilerErrorDescription;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DuplicateIdentifierError implements CompilerErrorDescription {
    private final String identifier;
    
    @Override
    public String describe() {
        return "The variable \"" + identifier + "\" has already been declared in this scope";
    }
}
