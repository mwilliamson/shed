package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class VariableLookup {
    public static TypeResult<Type> lookupVariableReference(String identifier, SourceRange nodeLocation, StaticContext context) {
        VariableLookupResult result = context.get(identifier);
        if (result.getStatus() == Status.SUCCESS) {
            return success(result.getType());
        } else if (result.getStatus() == Status.NOT_DECLARED_YET) {
            return failure(asList(CompilerError.error(
                nodeLocation,
                "Cannot access variable \"" + identifier + "\" before it is declared"
            )));
        } else {
            return failure(asList(CompilerError.error(
                nodeLocation,
                "No variable \"" + identifier + "\" in scope"
            )));
        }
    }
}
