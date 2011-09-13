package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.SimpleCompilerError;
import org.zwobble.shed.compiler.parsing.SourceRange;

public class TypeErrors {
    public static SimpleCompilerError duplicateIdentifierError(String identifier, SourceRange nodeLocation) {
        return new SimpleCompilerError(
            nodeLocation,
            "The variable \"" + identifier + "\" has already been declared in this scope"
        );
    }
}
