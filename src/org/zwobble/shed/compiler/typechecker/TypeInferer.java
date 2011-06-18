package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
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
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.types.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.types.VariableLookup.lookupVariableReference;

public class TypeInferer {
    public static TypeResult inferType(ExpressionNode expression, StaticContext context) {
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
            return lookupVariableReference(((VariableIdentifierNode)expression).getIdentifier(), context);
        }
        if (expression instanceof ShortLambdaExpressionNode) {
            return inferType((ShortLambdaExpressionNode)expression, context);
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }

    private static TypeResult inferType(ShortLambdaExpressionNode lambdaExpression, StaticContext context) {
        List<Type> typeParameters = new ArrayList<Type>();
        for (FormalArgumentNode argument : lambdaExpression.getArguments()) {
            TypeResult argumentType = lookupTypeReference(argument.getType(), context);
            if (!argumentType.isSuccess()) {
                throw new RuntimeException(argumentType.toString());
            }
            typeParameters.add(argumentType.get());
        }
        
        // TODO: add arguments to context
        TypeResult expressionTypeResult = inferType(lambdaExpression.getBody(), context);
        if (!expressionTypeResult.isSuccess()) {
            return expressionTypeResult;
        }
        Option<TypeReferenceNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResult returnType = lookupTypeReference(returnTypeReference.get(), context);
            if (!returnType.isSuccess()) {
                return returnType;
            }
            if (!expressionTypeResult.get().equals(returnType.get())) {
                return failure(asList(new CompilerError(
                    range(position(-1, -1), position(-1, -1)),
                    "Type mismatch: expected expression of type \"" + returnType.get().shortName() +
                        "\" but was of type \"" + expressionTypeResult.get().shortName() + "\""
                )));
            }
        }
        typeParameters.add(expressionTypeResult.get());
        return success((Type)new TypeApplication(CoreTypes.functionType(lambdaExpression.getArguments().size()), typeParameters));
    }
}
