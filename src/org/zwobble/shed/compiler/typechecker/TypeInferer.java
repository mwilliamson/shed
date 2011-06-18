package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableLookup.lookupVariableReference;

public class TypeInferer {
    public static TypeResult inferType(ExpressionNode expression, NodeLocations nodeLocations, StaticContext context) {
        if (expression instanceof BooleanLiteralNode) {
            return success(CoreTypes.BOOLEAN);            
        }
        if (expression instanceof NumberLiteralNode) {
            return success(CoreTypes.NUMBER);
        }
        if (expression instanceof StringLiteralNode) {
            return success(CoreTypes.STRING);
        }
        if (expression instanceof VariableIdentifierNode) {
            return lookupVariableReference(((VariableIdentifierNode)expression).getIdentifier(), nodeLocations.locate(expression), context);
        }
        if (expression instanceof ShortLambdaExpressionNode) {
            return inferType((ShortLambdaExpressionNode)expression, nodeLocations, context);
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }

    private static TypeResult inferType(ShortLambdaExpressionNode lambdaExpression, NodeLocations nodeLocations, StaticContext context) {
        List<Type> typeParameters = new ArrayList<Type>();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        for (FormalArgumentNode argument : lambdaExpression.getArguments()) {
            TypeResult argumentType = lookupTypeReference(argument.getType(), nodeLocations, context);
            errors.addAll(argumentType.getErrors());
            typeParameters.add(argumentType.get());
        }
        
        // TODO: add arguments to context
        TypeResult expressionTypeResult = inferType(lambdaExpression.getBody(), nodeLocations, context);
        errors.addAll(expressionTypeResult.getErrors());
        
        Option<TypeReferenceNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResult returnTypeResult = lookupTypeReference(returnTypeReference.get(), nodeLocations, context);
            errors.addAll(returnTypeResult.getErrors());
            if (returnTypeResult.isSuccess() && !expressionTypeResult.get().equals(returnTypeResult.get())) {
                errors.add(new CompilerError(
                    nodeLocations.locate(lambdaExpression.getBody()),
                    "Type mismatch: expected expression of type \"" + returnTypeResult.get().shortName() +
                        "\" but was of type \"" + expressionTypeResult.get().shortName() + "\""
                ));
            }
        }
        typeParameters.add(expressionTypeResult.get());
        
        if (!errors.isEmpty()) {
            return failure(errors);
        }
        
        return success(new TypeApplication(CoreTypes.functionType(lambdaExpression.getArguments().size()), typeParameters));
    }
}
