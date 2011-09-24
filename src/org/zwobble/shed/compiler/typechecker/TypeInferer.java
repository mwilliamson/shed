package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.AssignmentExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionWithBodyNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.UnitLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.errors.InvalidAssignmentError;
import org.zwobble.shed.compiler.typechecker.errors.MissingReturnStatementError;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.TypeFunction;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.ArgumentTypeInferer.inferArgumentTypesAndAddToContext;
import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
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
            return inferFunctionTypeAndTypeCheckBody((LongLambdaExpressionNode)expression, nodeLocations, context);
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
        TypeResult<List<Type>> result = inferArgumentTypesAndAddToContext(lambdaExpression.getFormalArguments(), nodeLocations, context);
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
                                return failure(new CompilerError(
                                    nodeLocations.locate(lambdaExpression.getBody()),
                                    new TypeMismatchError(returnType, expressionType)
                                ));
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

    public static TypeResult<ValueInfo>
    inferFunctionTypeAndTypeCheckBody(final FunctionWithBodyNode function, final NodeLocations nodeLocations, final StaticContext context) {
        TypeResult<ValueInfo> typeResult = inferFunctionType(function, nodeLocations, context);
        return typeResult.ifValueThen(typeCheckBody(function, nodeLocations, context));
    }
    
    public static TypeResult<ValueInfo> inferFunctionType(
        final FunctionWithBodyNode function, final NodeLocations nodeLocations, final StaticContext context
    ) {
        final TypeResult<List<Type>> argumentTypeResults = inferArgumentTypesAndAddToContext(function.getFormalArguments(), nodeLocations, context);
        TypeResult<Type> returnTypeResult = lookupTypeReference(function.getReturnType(), nodeLocations, context);
        
        return returnTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type returnType) {
                return argumentTypeResults.ifValueThen(buildFunctionType(returnType));
            }
        }).ifValueThen(toValueInfo());
    }

    public static Function<ValueInfo, TypeResult<ValueInfo>> typeCheckBody(
        final FunctionWithBodyNode function, final NodeLocations nodeLocations,final StaticContext context
    ) {
        return new Function<ValueInfo, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(ValueInfo returnTypeInfo) {
                List<Type> functionTypeParameters = ((TypeApplication)returnTypeInfo.getType()).getTypeParameters();
                Type returnType = functionTypeParameters.get(functionTypeParameters.size() - 1);
                TypeResult<ValueInfo> result = success(returnTypeInfo);

                TypeResult<StatementTypeCheckResult> blockResult = 
                    new BlockTypeChecker().typeCheckBlock(function.getBody(), context, nodeLocations, Option.some(returnType));
                
                result = result.withErrorsFrom(blockResult);
                
                if (!blockResult.get().hasReturned()) {
                    result = result.withErrorsFrom(TypeResult.<Type>failure(new CompilerError(
                        nodeLocations.locate(function),
                        new MissingReturnStatementError()
                    )));
                }
                return result;
            }
        };
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
        return inferType(memberAccess.getExpression(), nodeLocations, context).ifValueThen(new Function<Type, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(Type leftType) {
                String name = memberAccess.getMemberName();
                Map<String, ValueInfo> members = ((ScalarType)leftType).getMembers();
                
                if (members.containsKey(name)) {
                    return TypeResult.success(members.get(name));
                } else {
                    return TypeResult.failure(asList(CompilerError.error(nodeLocations.locate(memberAccess), "No such member: " + name)));
                }
            }
        });
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
        SourceRange location = nodeLocations.locate(expression);
        TypeResult<Type> valueTypeResult = inferType(expression.getValue(), nodeLocations, context);
        TypeResult<ValueInfo> targetInfo = inferValueInfo(expression.getTarget(), nodeLocations, context)
            .ifValueThen(checkIsAssignable(location));
        
        TypeResult<ValueInfo> result = valueTypeResult.withErrorsFrom(targetInfo).ifValueThen(toValueInfo());
        if (valueTypeResult.hasValue() && targetInfo.hasValue()) {
            Type valueType = valueTypeResult.get();
            Type targetType = targetInfo.get().getType();
            if (!isSubType(valueType, targetType)) {
                result = result.withErrorsFrom(failure(new CompilerError(location, new TypeMismatchError(targetType, valueType))));
            }
        }
        return result;
    }
    
    private static Function<ValueInfo, TypeResult<ValueInfo>> checkIsAssignable(final SourceRange location) {
        return new Function<ValueInfo, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(ValueInfo input) {
                if (input.isAssignable()) {
                    return TypeResult.success(input);
                } else {
                    return TypeResult.failure(input, new CompilerError(location, new InvalidAssignmentError()));
                }
            }
        };
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

    private static Function<List<Type>, TypeResult<Type>> buildFunctionType(final Type returnType) {
        return new Function<List<Type>, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(List<Type> argumentTypes) {
                List<Type> typeParameters = new ArrayList<Type>(argumentTypes);
                typeParameters.add(returnType);
                return success(TypeApplication.applyTypes(CoreTypes.functionType(argumentTypes.size()), typeParameters));
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
