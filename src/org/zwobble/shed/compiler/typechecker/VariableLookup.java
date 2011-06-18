package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class VariableLookup {
    public static TypeResult lookupVariableReference(String identifier, SourceRange nodeLocation, StaticContext context) {
        Option<Type> type = context.get(identifier);
        if (type.hasValue()) {
            return success(type.get());
        } else {
            return failure(asList(new CompilerError(
                nodeLocation,
                "No variable \"" + identifier + "\" in scope"
            )));
        }
    }
}
