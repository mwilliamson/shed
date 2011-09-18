package org.zwobble.shed.compiler.referenceresolution;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.SourceRange;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class VariableNotInScopeError implements CompilerError {
    private final String identifier;
    @Getter
    private final SourceRange location;

    @Override
    public String getDescription() {
        return "No variable \"" + identifier + "\" in scope";
    }
}
