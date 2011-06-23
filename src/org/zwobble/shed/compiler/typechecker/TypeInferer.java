package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static com.google.common.collect.Lists.transform;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.TypeChecker.typeCheckStatement;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.combine;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableLookup.lookupVariableReference;

public class TypeInferer {
    public static TypeResult<Type> inferType(ExpressionNode expression, NodeLocations nodeLocations, StaticContext context) {
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
        if (expression instanceof LongLambdaExpressionNode) {
            return inferLongLambdaExpressionType((LongLambdaExpressionNode)expression, nodeLocations, context);
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }

    private static TypeResult<Type> inferType(ShortLambdaExpressionNode lambdaExpression, NodeLocations nodeLocations, StaticContext context) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        context.enterNewScope(none(Type.class));
        
        List<TypeResult<FormalArgumentType>> argumentTypesResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), nodeLocations, context);

        for (TypeResult<FormalArgumentType> argumentTypeResult : argumentTypesResult) {
            argumentTypeResult.ifValueThen(addArgumentToContext(context));
            errors.addAll(argumentTypeResult.getErrors());
        }
        
        TypeResult<Type> expressionTypeResult = inferType(lambdaExpression.getBody(), nodeLocations, context);
        errors.addAll(expressionTypeResult.getErrors());
        context.exitScope();
        
        Option<TypeReferenceNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResult<Type> returnTypeResult = lookupTypeReference(returnTypeReference.get(), nodeLocations, context);
            errors.addAll(returnTypeResult.getErrors());
            if (returnTypeResult.hasValue() && expressionTypeResult.hasValue() && !expressionTypeResult.get().equals(returnTypeResult.get())) {
                errors.add(new CompilerError(
                    nodeLocations.locate(lambdaExpression.getBody()),
                    "Type mismatch: expected expression of type \"" + returnTypeResult.get().shortName() +
                        "\" but was of type \"" + expressionTypeResult.get().shortName() + "\""
                ));
            }
        }
        
        if (!errors.isEmpty()) {
            return failure(errors);
        }
        return combine(argumentTypesResult).ifValueThen(buildFunctionType(expressionTypeResult.get()));
    }

    private static TypeResult<Type>
    inferLongLambdaExpressionType(final LongLambdaExpressionNode lambdaExpression, final NodeLocations nodeLocations, final StaticContext context) {
        final List<TypeResult<FormalArgumentType>> argumentTypesResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), nodeLocations, context);
        TypeResult<Type> returnTypeResult = lookupTypeReference(lambdaExpression.getReturnType(), nodeLocations, context);
        return returnTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type returnType) {
                context.enterNewScope(some(returnType));
                for (TypeResult<FormalArgumentType> argumentTypeResult : argumentTypesResult) {
                    argumentTypeResult.ifValueThen(addArgumentToContext(context));
                }
                TypeResult<Type> result = combine(argumentTypesResult)
                    .ifValueThen(buildFunctionType(returnType));
                
                for (StatementNode statement : lambdaExpression.getBody()) {
                    TypeResult<Void> statementResult = typeCheckStatement(statement, nodeLocations, context);
                    result = result.withErrorsFrom(statementResult);
                }
                context.exitScope();
                return result;
            }
        });
    }
    
    private static Function<List<FormalArgumentType>, TypeResult<Type>> buildFunctionType(final Type returnType) {
        return new Function<List<FormalArgumentType>, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(List<FormalArgumentType> argumentTypes) {
                List<Type> typeParameters = new ArrayList<Type>(transform(argumentTypes, toType()));
                typeParameters.add(returnType);
                return success((Type)new TypeApplication(CoreTypes.functionType(argumentTypes.size()), typeParameters));
            }
        };
    }

    private static Function<FormalArgumentType, Type> toType() {
        return new Function<TypeInferer.FormalArgumentType, Type>() {
            @Override
            public Type apply(FormalArgumentType argument) {
                return argument.getType();
            }
        };
    }

    private static List<TypeResult<FormalArgumentType>>
    inferArgumentTypes(List<FormalArgumentNode> formalArguments, NodeLocations nodeLocations, StaticContext context) {
        return transform(formalArguments, inferArgumentType(nodeLocations, context));
    }

    private static Function<FormalArgumentNode, TypeResult<FormalArgumentType>>
    inferArgumentType(final NodeLocations nodeLocations, final StaticContext context) {
        return new Function<FormalArgumentNode, TypeResult<FormalArgumentType>>() {
            @Override
            public TypeResult<FormalArgumentType> apply(FormalArgumentNode argument) {
                TypeResult<Type> lookupTypeReference = lookupTypeReference(argument.getType(), nodeLocations, context);
                return lookupTypeReference.ifValueThen(buildFormalArgumentType(argument.getName()));
            }
        };
    }
    
    private static Function<Type, TypeResult<FormalArgumentType>> buildFormalArgumentType(final String name) {
        return new Function<Type, TypeResult<FormalArgumentType>>() {
            @Override
            public TypeResult<FormalArgumentType> apply(Type type) {
                return success(new FormalArgumentType(name, type));
            }
        };
    }

    @Data
    private static class FormalArgumentType {
        private final String name;
        private final Type type;
    }

    private static Function<FormalArgumentType, TypeResult<Void>> addArgumentToContext(final StaticContext context) {
        return new Function<FormalArgumentType, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(FormalArgumentType argument) {
                context.add(argument.getName(), argument.getType());
                return success(null);
            }
        };
    }
}
