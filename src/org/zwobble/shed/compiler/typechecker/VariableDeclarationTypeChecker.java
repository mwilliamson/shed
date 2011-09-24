package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;
import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class VariableDeclarationTypeChecker implements StatementTypeChecker<VariableDeclarationNode> {
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        VariableDeclarationNode variableDeclaration, NodeLocations nodeLocations, StaticContext staticContext, Option<Type> returnType
    ) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        TypeResult<Type> valueTypeResult = inferType(variableDeclaration.getValue(), nodeLocations, staticContext);
        errors.addAll(valueTypeResult.getErrors());
        
        if (variableDeclaration.getTypeReference().hasValue()) {
            ExpressionNode typeReference = variableDeclaration.getTypeReference().get();
            TypeResult<Type> typeResult = lookupTypeReference(typeReference, nodeLocations, staticContext);
            errors.addAll(typeResult.getErrors());
            if (errors.isEmpty() && !isSubType(valueTypeResult.get(), typeResult.get())) {
                errors.add(CompilerError.error(
                    nodeLocations.locate(variableDeclaration.getValue()),
                    "Cannot initialise variable of type \"" + typeResult.get().shortName() +
                        "\" with expression of type \"" + valueTypeResult.get().shortName() + "\""
                ));
            }
        }
        
        if (errors.isEmpty()) {
            Type type = valueTypeResult.get();
            ValueInfo valueInfo = variableDeclaration.isMutable()
                ? ValueInfo.assignableValue(type)
                : unassignableValue(type);
                
            staticContext.add(variableDeclaration, valueInfo);
            return success(StatementTypeCheckResult.noReturn());
        } else {
            return failure(errors);
        }
    }
}
