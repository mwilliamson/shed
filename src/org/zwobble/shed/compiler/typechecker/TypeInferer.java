package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.Result;
import org.zwobble.shed.compiler.parsing.SourcePosition;
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
import static org.zwobble.shed.compiler.parsing.Result.fatal;
import static org.zwobble.shed.compiler.parsing.Result.success;
import static org.zwobble.shed.compiler.types.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.types.VariableLookup.lookupVariableReference;

public class TypeInferer {
    public static Result<Type> inferType(ExpressionNode expression, StaticContext context) {
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

    private static Result<Type> inferType(ShortLambdaExpressionNode lambdaExpression, StaticContext context) {
        List<Type> typeParameters = new ArrayList<Type>();
        for (FormalArgumentNode argument : lambdaExpression.getArguments()) {
            Result<Type> argumentType = lookupTypeReference(argument.getType(), context);
            if (argumentType.anyErrors()) {
                throw new RuntimeException(argumentType.toString());
            }
            typeParameters.add(argumentType.get());
        }
        
        // TODO: add arguments to context
        Result<Type> expressionTypeResult = inferType(lambdaExpression.getBody(), context);
        if (expressionTypeResult.anyErrors()) {
            return expressionTypeResult.changeValue(null);
        }
        Option<TypeReferenceNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            Result<Type> returnType = lookupTypeReference(returnTypeReference.get(), context);
            if (returnType.anyErrors()) {
                return returnType;
            }
            if (!expressionTypeResult.get().equals(returnType.get())) {
                return fatal(asList(new CompilerError(
                    new SourcePosition(-1, -1),
                    new SourcePosition(-1, -1),
                    "Type mismatch: expected expression of type \"" + returnType.get().shortName() +
                        "\" but was of type \"" + expressionTypeResult.get().shortName() + "\""
                )));
            }
        }
        typeParameters.add(expressionTypeResult.get());
        return success((Type)new TypeApplication(CoreTypes.functionType(lambdaExpression.getArguments().size()), typeParameters));
    }
}
