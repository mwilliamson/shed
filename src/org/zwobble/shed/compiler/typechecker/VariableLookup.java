package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class VariableLookup {
    public static TypeResult<ValueInfo> lookupVariableReference(VariableIdentifierNode reference, SourceRange nodeLocation, StaticContext context) {
        VariableLookupResult result = context.get(reference);
        if (result.getStatus() == Status.SUCCESS) {
            return success(ValueInfo.unassignableValue(result.getType()));
        } else {
            return failure(asList(new CompilerError(
                nodeLocation,
                new UntypedReferenceError(reference.getIdentifier())
            )));
        }
    }
}
