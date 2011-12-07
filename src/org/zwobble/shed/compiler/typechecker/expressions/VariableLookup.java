package org.zwobble.shed.compiler.typechecker.expressions;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

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
        Option<ValueInfo> result = context.getValueInfoFor(references.findReferent(reference));
        if (result.hasValue()) {
            return success(result.get());
        } else {
            return failure(error(
                reference,
                new UntypedReferenceError(reference.getIdentifier())
            ));
        }
    }
}
