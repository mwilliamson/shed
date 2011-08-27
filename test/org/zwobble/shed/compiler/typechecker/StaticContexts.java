package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeErrors.duplicateIdentifierError;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class StaticContexts {
    public static TypeResult<Void> tryAdd(StaticContext context, String identifier, Type type, SourceRange nodeLocation) {
        if (context.isDeclaredInCurrentScope(identifier)) {
            return failure(asList(duplicateIdentifierError(identifier, nodeLocation)));
        } else {
            context.add(identifier, type);
            return success(null);
        }
    }
}
