package org.zwobble.shed.compiler.types;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.Result;
import org.zwobble.shed.compiler.parsing.SourcePosition;
import org.zwobble.shed.compiler.typechecker.StaticContext;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.Result.fatal;
import static org.zwobble.shed.compiler.parsing.Result.success;

public class VariableLookup {
    public static Result<Type> lookupVariableReference(String identifier, StaticContext context) {
        Option<Type> type = context.get(identifier);
        if (type.hasValue()) {
            return success(type.get());
        } else {
            return fatal(asList(new CompilerError(
                new SourcePosition(-1, -1),
                new SourcePosition(-1, -1),
                "No variable \"" + identifier + "\" in scope"
            )));
        }
    }
}
