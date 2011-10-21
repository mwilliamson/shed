package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class VariableLookup {
    public static TypeResult<ValueInfo> lookupVariableReference(VariableIdentifierNode reference, StaticContext context) {
        VariableLookupResult result = context.get(reference);
        if (result.getStatus() == Status.SUCCESS) {
            return success(result.getValueInfo());
        } else {
            return failure(error(
                reference,
                new UntypedReferenceError(reference.getIdentifier())
            ));
        }
    }
}
