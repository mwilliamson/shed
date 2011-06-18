package org.zwobble.shed.compiler.types;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class VariableLookup {
    public static TypeResult lookupVariableReference(String identifier, StaticContext context) {
        Option<Type> type = context.get(identifier);
        if (type.hasValue()) {
            return success(type.get());
        } else {
            return failure(asList(new CompilerError(
                range(position(-1, -1), position(-1, -1)),
                "No variable \"" + identifier + "\" in scope"
            )));
        }
    }
}
