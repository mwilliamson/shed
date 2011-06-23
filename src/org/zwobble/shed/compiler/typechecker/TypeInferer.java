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

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
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

    private static TypeResult<Type> inferType(final ShortLambdaExpressionNode lambdaExpression, final NodeLocations nodeLocations, StaticContext context) {
        List<TypeResult<FormalArgumentType>> argumentTypesResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), nodeLocations, context);

        context.enterNewScope(none(Type.class));
        for (TypeResult<FormalArgumentType> argumentTypeResult : argumentTypesResult) {
            argumentTypeResult.ifValueThen(addArgumentToContext(context));
        }
        final TypeResult<Type> expressionTypeResult = inferType(lambdaExpression.getBody(), nodeLocations, context);
        context.exitScope();
        
        TypeResult<List<FormalArgumentType>> result = combine(argumentTypesResult).withErrorsFrom(expressionTypeResult);
        
        Option<TypeReferenceNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResult<Type> returnTypeResult = lookupTypeReference(returnTypeReference.get(), nodeLocations, context);
            result = result.withErrorsFrom(returnTypeResult.ifValueThen(new Function<Type, TypeResult<Void>>() {
                @Override
                public TypeResult<Void> apply(final Type returnType) {
                    return expressionTypeResult.use(new Function<Type, TypeResult<Void>>() {
                        @Override
                        public TypeResult<Void> apply(Type expressionType) {
                            if (expressionType.equals(returnType)) {
                                return success(null);
                            } else {
                                return failure(asList(new CompilerError(
                                    nodeLocations.locate(lambdaExpression.getBody()),
                                    "Type mismatch: expected expression of type \"" + returnType.shortName() +
                                        "\" but was of type \"" + expressionType.shortName() + "\""
                                )));
                            }
                        }
                    });
                }
            }));
        }
        
        return result.ifValueThen(buildFunctionType(expressionTypeResult.get()));
    }

    private static TypeResult<Type>
    inferLongLambdaExpressionType(final LongLambdaExpressionNode lambdaExpression, final NodeLocations nodeLocations, final StaticContext context) {
        final List<TypeResult<FormalArgumentType>> argumentTypesResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), nodeLocations, context);
        TypeResult<Type> returnTypeResult = lookupTypeReference(lambdaExpression.getReturnType(), nodeLocations, context);
        TypeResult<Void> bodyResult = returnTypeResult.use(new Function<Type, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(Type returnType) {
                context.enterNewScope(some(returnType));
                for (TypeResult<FormalArgumentType> argumentTypeResult : argumentTypesResult) {
                    argumentTypeResult.ifValueThen(addArgumentToContext(context));
                }
                
                TypeResult<Void> result = success(null);
                for (StatementNode statement : lambdaExpression.getBody()) {
                    TypeResult<Void> statementResult = typeCheckStatement(statement, nodeLocations, context);
                    result = result.withErrorsFrom(statementResult);
                }
                context.exitScope();
                return result;
            }
        });
        
        return returnTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type returnType) {
                return combine(argumentTypesResult).ifValueThen(buildFunctionType(returnType));
            }
        }).withErrorsFrom(bodyResult);
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
