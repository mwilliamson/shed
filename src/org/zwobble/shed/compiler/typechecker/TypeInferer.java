package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.Result;
import org.zwobble.shed.compiler.parsing.SourcePosition;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.Result.fatal;
import static org.zwobble.shed.compiler.parsing.Result.success;

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
            ShortLambdaExpressionNode lambdaExpression = (ShortLambdaExpressionNode)expression;
            Result<Type> expressionTypeResult = inferType(lambdaExpression.getBody(), context);
            if (expressionTypeResult.anyErrors()) {
                return expressionTypeResult.changeValue(null);
            }
            Option<TypeReferenceNode> returnTypeReference = lambdaExpression.getReturnType();
            if (returnTypeReference.hasValue()) {
                Result<Type> returnType = lookupTypeReference(returnTypeReference.get(), context);
                if (!expressionTypeResult.get().equals(returnType.get())) {
                    return fatal(asList(new CompilerError(
                        new SourcePosition(-1, -1),
                        new SourcePosition(-1, -1),
                        "Type mismatch: expected expression of type \"" + returnType.get().shortName() +
                            "\" but was of type \"" + expressionTypeResult.get().shortName() + "\""
                    )));
                }
            }
            return success((Type)new TypeApplication(CoreTypes.functionType(), asList(expressionTypeResult.get())));
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }
    
    private static Result<Type> lookupVariableReference(String identifier, StaticContext context) {
        Option<Type> type = context.get(identifier);
        if (type.hasValue()) {
            return success(type.get());
        } else {
            return fatal(asList(new CompilerError(
                new SourcePosition(-1, -1),
                new SourcePosition(-1, -1),
                "No variable \"" + identifier + "\" in scope"
            )));
        }
    }
    
    private static Result<Type> lookupTypeReference(TypeReferenceNode typeReference, StaticContext context) {
        if (typeReference instanceof TypeIdentifierNode) {
            String identifier = ((TypeIdentifierNode)typeReference).getIdentifier();
            Result<Type> variableType = lookupVariableReference(identifier, context);
            if (variableType.hasValue()) {
                return success(((TypeApplication)variableType.get()).getTypeParameters().get(0));
            } else {
                return variableType;
            }
        }
        throw new RuntimeException("Cannot look up type reference: " + typeReference);
    }
}
