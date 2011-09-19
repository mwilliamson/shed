package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class VariableLookup {
    public static TypeResult<Type> lookupVariableReference(VariableIdentifierNode reference, SourceRange nodeLocation, StaticContext context) {
        VariableLookupResult result = context.get(reference);
        if (result.getStatus() == Status.SUCCESS) {
            return success(result.getType());
        } else {
            return failure(asList(new CompilerError(
                nodeLocation,
                new UntypedReferenceError(reference.getIdentifier())
            )));
        }
    }
}
