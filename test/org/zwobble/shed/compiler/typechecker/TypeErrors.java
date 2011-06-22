package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.SourceRange;

public class TypeErrors {
    public static CompilerError duplicateIdentifierError(String identifier, SourceRange nodeLocation) {
        return new CompilerError(
            nodeLocation,
            "The variable \"" + identifier + "\" has already been declared in this scope"
        );
    }
}
