package org.zwobble.shed.compiler.typechecker.expressions;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;

import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;

import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

import static org.zwobble.shed.compiler.CompilerErrors.error;

public class VariableLookup implements ExpressionTypeInferer<VariableIdentifierNode> {
    private final References references;
    private final StaticContext context;

    @Inject
    public VariableLookup(References references, StaticContext context) {
        this.references = references;
        this.context = context;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(VariableIdentifierNode reference) {
        VariableLookupResult result = context.get(references.findReferent(reference));
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