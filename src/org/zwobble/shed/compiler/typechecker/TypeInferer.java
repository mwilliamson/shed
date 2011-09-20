package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.AssignmentExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.UnitLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.TypeFunction;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.combine;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableLookup.lookupVariableReference;

public class TypeInferer {
    public static TypeResult<ValueInfo> inferValueInfo(ExpressionNode expression, NodeLocations nodeLocations, StaticContext context) {
        if (expression instanceof BooleanLiteralNode) {
            return success(ValueInfo.unassignableValue(CoreTypes.BOOLEAN));            
        }
        if (expression instanceof NumberLiteralNode) {
            return success(ValueInfo.unassignableValue(CoreTypes.NUMBER));
        }
        if (expression instanceof StringLiteralNode) {
            return success(ValueInfo.unassignableValue(CoreTypes.STRING));
        }
        if (expression instanceof UnitLiteralNode) {
            return success(ValueInfo.unassignableValue(CoreTypes.UNIT));
        }
        if (expression instanceof VariableIdentifierNode) {
            return lookupVariableReference((VariableIdentifierNode)expression, nodeLocations.locate(expression), context);
        }
        if (expression instanceof ShortLambdaExpressionNode) {
            return inferType((ShortLambdaExpressionNode)expression, nodeLocations, context);
        }
        if (expression instanceof LongLambdaExpressionNode) {
            return inferLongLambdaExpressionType((LongLambdaExpressionNode)expression, nodeLocations, context);
        }
        if (expression instanceof CallNode) {
            return inferCallType((CallNode)expression, nodeLocations, context);
        }
        if (expression instanceof MemberAccessNode) {
            return inferMemberAccessType((MemberAccessNode)expression, nodeLocations, context);
        }
        if (expression instanceof TypeApplicationNode) {
            return inferTypeApplicationType((TypeApplicationNode)expression, nodeLocations, context);
        }
        if (expression instanceof AssignmentExpressionNode) {
            return inferAssignmentType((AssignmentExpressionNode)expression, nodeLocations, context);
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }
    
    public static TypeResult<Type> inferType(ExpressionNode expression, NodeLocations nodeLocations, StaticContext context) {
        return inferValueInfo(expression, nodeLocations, context).ifValueThen(new Function<ValueInfo, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(ValueInfo input) {
                return success(input.getType());
            }
        });
    }

    private static TypeResult<ValueInfo> inferType(final ShortLambdaExpressionNode lambdaExpression, final NodeLocations nodeLocations, StaticContext context) {
        List<TypeResult<FormalArgumentType>> argumentTypesResult = inferArgumentTypes(lambdaExpression.getFormalArguments(), nodeLocations, context);
        TypeResult<List<FormalArgumentType>> result = combine(argumentTypesResult);
        
        for (TypeResult<FormalArgumentType> argumentTypeResult : argumentTypesResult) {
            TypeResult<Void> addArgumentToContextResult = argumentTypeResult.use(addArgumentToContext(context, nodeLocations));
            result = result.withErrorsFrom(addArgumentToContextResult);
        }
        final TypeResult<Type> expressionTypeResult = inferType(lambdaExpression.getBody(), nodeLocations, context);
        
        result = result.withErrorsFrom(expressionTypeResult);
        
        Option<? extends ExpressionNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResult<Type> returnTypeResult = lookupTypeReference(returnTypeReference.get(), nodeLocations, context);
            result = result.withErrorsFrom(returnTypeResult.ifValueThen(new Function<Type, TypeResult<Void>>() {
                @Override
                public TypeResult<Void> apply(final Type returnType) {
                    return expressionTypeResult.use(new Function<Type, TypeResult<Void>>() {
                        @Override
                        public TypeResult<Void> apply(Type expressionType) {
                            if (expressionType.equals(returnType)) {
                                return success();
                            } else {
                                return failure(asList(CompilerError.error(
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
        
        if (expressionTypeResult.hasValue()) {
            return result.ifValueThen(buildFunctionType(expressionTypeResult.get())).ifValueThen(toValueInfo());            
        } else {
            return TypeResult.<ValueInfo>failure(result.getErrors());
        }
    }

    private static TypeResult<ValueInfo>
    inferLongLambdaExpressionType(final LongLambdaExpressionNode lambdaExpression, final NodeLocations nodeLocations, final StaticContext context) {
        final List<TypeResult<FormalArgumentType>> argumentTypeResults = inferArgumentTypes(lambdaExpression.getFormalArguments(), nodeLocations, context);
        final TypeResult<List<FormalArgumentType>> combinedArgumentTypesResult = combine(argumentTypeResults);
        TypeResult<Type> returnTypeResult = lookupTypeReference(lambdaExpression.getReturnType(), nodeLocations, context);

        TypeResult<?> result = combinedArgumentTypesResult.withErrorsFrom(returnTypeResult);
        TypeResult<Void> bodyResult = returnTypeResult.use(new Function<Type, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(Type returnType) {
                TypeResult<Void> result = success();

                for (TypeResult<FormalArgumentType> argumentTypeResult : argumentTypeResults) {
                    TypeResult<Void> addArgumentToContextResult = argumentTypeResult.use(addArgumentToContext(context, nodeLocations));
                    result = result.withErrorsFrom(addArgumentToContextResult);
                }
                
                TypeResult<StatementTypeCheckResult> blockResult = 
                    new BlockTypeChecker().typeCheckBlock(lambdaExpression.getBody(), context, nodeLocations, Option.some(returnType));
                
                result = result.withErrorsFrom(blockResult);
                
                if (!blockResult.get().hasReturned()) {
                    result = result.withErrorsFrom(TypeResult.<Type>failure(asList(CompilerError.error(
                        nodeLocations.locate(lambdaExpression),
                        "Expected return statement"
                    ))));
                }
                return result;
            }
        });
        result = result.withErrorsFrom(bodyResult);
        
        return returnTypeResult.use(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type returnType) {
                return combinedArgumentTypesResult.use(buildFunctionType(returnType));
            }
        }).withErrorsFrom(result).ifValueThen(toValueInfo());
    }

    private static TypeResult<ValueInfo> inferCallType(final CallNode expression, final NodeLocations nodeLocations, final StaticContext context) {
        TypeResult<Type> calledTypeResult = inferType(expression.getFunction(), nodeLocations, context);
        return calledTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type calledType) {
                if (!CoreTypes.isFunction(calledType)) {
                    CompilerError error = CompilerError.error(nodeLocations.locate(expression), "Cannot call objects that aren't functions");
                    return TypeResult.failure(asList(error));
                }
                TypeApplication functionType = (TypeApplication)calledType;
                final List<Type> typeParameters = functionType.getTypeParameters();
                
                int numberOfFormalAguments = typeParameters.size() - 1;
                int numberOfActualArguments = expression.getArguments().size();
                if (numberOfFormalAguments != numberOfActualArguments) {
                    String errorMessage = "Function requires " + numberOfFormalAguments + " argument(s), but is called with " + numberOfActualArguments;
                    CompilerError error = CompilerError.error(nodeLocations.locate(expression), errorMessage);
                    return TypeResult.failure(asList(error));
                }
                
                TypeResult<Type> result = success(typeParameters.get(numberOfFormalAguments));
                for (int i = 0; i < numberOfFormalAguments; i++) {
                    final int index = i;
                    final ExpressionNode argument = expression.getArguments().get(i);
                    result = result.withErrorsFrom(inferType(argument, nodeLocations, context).ifValueThen(new Function<Type, TypeResult<Void>>() {
                        @Override
                        public TypeResult<Void> apply(Type actualArgumentType) {
                            if (isSubType(actualArgumentType, typeParameters.get(index))) {
                                return success();
                            } else {
                                return failure(asList(CompilerError.error(
                                    nodeLocations.locate(argument),
                                    "Expected expression of type " + typeParameters.get(index).shortName() +
                                        " as argument " + (index + 1) + ", but got expression of type " + actualArgumentType.shortName()
                                )));
                            }
                        }
                    }));
                }
                return result;
            }
        }).ifValueThen(toValueInfo());
    }

    private static TypeResult<ValueInfo> inferMemberAccessType(
        final MemberAccessNode memberAccess,
        final NodeLocations nodeLocations,
        StaticContext context
    ) {
        return inferType(memberAccess.getExpression(), nodeLocations, context).ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type leftType) {
                String name = memberAccess.getMemberName();
                Map<String, Type> members = ((ScalarType)leftType).getMembers();
                
                if (members.containsKey(name)) {
                    return TypeResult.success(members.get(name));
                } else {
                    return TypeResult.failure(asList(CompilerError.error(nodeLocations.locate(memberAccess), "No such member: " + name)));
                }
            }
        }).ifValueThen(toValueInfo());
    }

    private static TypeResult<ValueInfo> inferTypeApplicationType(
        final TypeApplicationNode typeApplication,
        final NodeLocations nodeLocations,
        final StaticContext context
    ) {
        return inferType(typeApplication.getBaseValue(), nodeLocations, context).ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type baseType) {
                List<Type> parameterTypes = Lists.transform(typeApplication.getParameters(), toParameterType(nodeLocations, context));
                
                if (baseType instanceof ParameterisedFunctionType) {
                    return TypeResult.success(TypeApplication.applyTypes((TypeFunction)baseType, parameterTypes));
                } else if (baseType instanceof ParameterisedType) {
                    return TypeResult.success((Type)CoreTypes.classOf(TypeApplication.applyTypes((ParameterisedType)baseType, parameterTypes)));   
                } else {
                    throw new RuntimeException("Don't know how to apply types " + parameterTypes + " to " + baseType);
                }
            }
        }).ifValueThen(toValueInfo());
    }

    private static TypeResult<ValueInfo> inferAssignmentType(
        AssignmentExpressionNode expression, NodeLocations nodeLocations, StaticContext context
    ) {
        return inferType(expression.getValue(), nodeLocations, context).ifValueThen(toValueInfo());
    }
    
    private static Function<ExpressionNode, Type> toParameterType(final NodeLocations nodeLocations, final StaticContext context) {
        return new Function<ExpressionNode, Type>() {
            @Override
            public Type apply(ExpressionNode expression) {
                TypeResult<Type> result = TypeLookup.lookupTypeReference(expression, nodeLocations, context);
                if (!result.isSuccess()) {
                    throw new RuntimeException(result.getErrors().toString());
                }
                return result.get();
            }
        };
    }

    private static Function<List<FormalArgumentType>, TypeResult<Type>> buildFunctionType(final Type returnType) {
        return new Function<List<FormalArgumentType>, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(List<FormalArgumentType> argumentTypes) {
                List<Type> typeParameters = new ArrayList<Type>(transform(argumentTypes, toType()));
                typeParameters.add(returnType);
                return success(TypeApplication.applyTypes(CoreTypes.functionType(argumentTypes.size()), typeParameters));
            }
        };
    }

    private static Function<FormalArgumentType, Type> toType() {
        return new Function<FormalArgumentType, Type>() {
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
                return lookupTypeReference.ifValueThen(buildFormalArgumentType(argument));
            }
        };
    }
    
    private static Function<Type, TypeResult<FormalArgumentType>> buildFormalArgumentType(final FormalArgumentNode node) {
        return new Function<Type, TypeResult<FormalArgumentType>>() {
            @Override
            public TypeResult<FormalArgumentType> apply(Type type) {
                return success(new FormalArgumentType(node.getIdentifier(), type, node));
            }
        };
    }

    private static Function<FormalArgumentType, TypeResult<Void>>
    addArgumentToContext(final StaticContext context, final NodeLocations nodeLocations) {
        return new Function<FormalArgumentType, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(FormalArgumentType argument) {
                context.add(argument.getNode(), unassignableValue(argument.getType()));
                return success();
            }
        };
    }

    private static Function<Type, TypeResult<ValueInfo>> toValueInfo() {
        return new Function<Type, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(Type input) {
                return success(unassignableValue(input));
            }
        };
    }
}
