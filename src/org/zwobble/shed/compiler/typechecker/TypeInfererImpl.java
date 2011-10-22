package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.CompilerErrors;
import org.zwobble.shed.compiler.Eager;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.AssignmentExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
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
import org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.TypeReplacer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class TypeInfererImpl implements TypeInferer {
    private final ArgumentTypeInferer argumentTypeInferer;
    private final BlockTypeChecker blockTypeChecker;
    private final TypeLookup typeLookup;
    private final VariableLookup variableLookup;
    private final StaticContext context;

    @Inject
    public TypeInfererImpl(
        ArgumentTypeInferer argumentTypeInferer,
        BlockTypeChecker blockTypeChecker, 
        TypeLookup typeLookup, 
        VariableLookup variableLookup,
        StaticContext context
    ) {
        this.argumentTypeInferer = argumentTypeInferer;
        this.blockTypeChecker = blockTypeChecker;
        this.typeLookup = typeLookup;
        this.variableLookup = variableLookup;
        this.context = context;
    }
    
    public TypeResult<ValueInfo> inferValueInfo(ExpressionNode expression) {
        if (expression instanceof BooleanLiteralNode) {
            return success(ValueInfo.unassignableValue(CoreTypes.BOOLEAN));            
        }
        if (expression instanceof NumberLiteralNode) {
            return success(ValueInfo.unassignableValue(CoreTypes.DOUBLE));
        }
        if (expression instanceof StringLiteralNode) {
            return success(ValueInfo.unassignableValue(CoreTypes.STRING));
        }
        if (expression instanceof UnitLiteralNode) {
            return success(ValueInfo.unassignableValue(CoreTypes.UNIT));
        }
        if (expression instanceof VariableIdentifierNode) {
            return variableLookup.lookupVariableReference((VariableIdentifierNode)expression);
        }
        if (expression instanceof ShortLambdaExpressionNode) {
            return inferType((ShortLambdaExpressionNode)expression);
        }
        if (expression instanceof LongLambdaExpressionNode) {
            return inferFunctionTypeAndTypeCheckBody((LongLambdaExpressionNode)expression);
        }
        if (expression instanceof CallNode) {
            return inferCallType((CallNode)expression);
        }
        if (expression instanceof MemberAccessNode) {
            return inferMemberAccessType((MemberAccessNode)expression);
        }
        if (expression instanceof TypeApplicationNode) {
            return inferTypeApplicationType((TypeApplicationNode)expression);
        }
        if (expression instanceof AssignmentExpressionNode) {
            return inferAssignmentType((AssignmentExpressionNode)expression);
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }
    
    public TypeResult<Type> inferType(ExpressionNode expression) {
        return inferValueInfo(expression).ifValueThen(new Function<ValueInfo, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(ValueInfo input) {
                return success(input.getType());
            }
        });
    }

    private TypeResult<ValueInfo> inferType(final ShortLambdaExpressionNode lambdaExpression) {
        TypeResult<List<Type>> result = argumentTypeInferer.inferArgumentTypesAndAddToContext(lambdaExpression.getFormalArguments());
        final TypeResult<Type> expressionTypeResult = inferType(lambdaExpression.getBody());
        
        result = result.withErrorsFrom(expressionTypeResult);
        
        Option<? extends ExpressionNode> returnTypeReference = lambdaExpression.getReturnType();
        if (returnTypeReference.hasValue()) {
            TypeResult<Type> returnTypeResult = typeLookup.lookupTypeReference(returnTypeReference.get());
            result = result.withErrorsFrom(returnTypeResult.ifValueThen(new Function<Type, TypeResult<Void>>() {
                @Override
                public TypeResult<Void> apply(final Type returnType) {
                    return expressionTypeResult.use(new Function<Type, TypeResult<Void>>() {
                        @Override
                        public TypeResult<Void> apply(Type expressionType) {
                            if (expressionType.equals(returnType)) {
                                return success();
                            } else {
                                return failure(error(
                                    lambdaExpression.getBody(),
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

    public TypeResult<ValueInfo> inferFunctionTypeAndTypeCheckBody(final FunctionWithBodyNode function) {
        TypeResult<ValueInfo> typeResult = inferFunctionType(function);
        return typeResult.ifValueThen(typeCheckBody(function));
    }
    
    public TypeResult<ValueInfo> inferFunctionType(final FunctionWithBodyNode function) {
        final TypeResult<List<Type>> argumentTypeResults = argumentTypeInferer.inferArgumentTypesAndAddToContext(function.getFormalArguments());
        TypeResult<Type> returnTypeResult = typeLookup.lookupTypeReference(function.getReturnType());
        
        return returnTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type returnType) {
                return argumentTypeResults.ifValueThen(buildFunctionType(returnType));
            }
        }).ifValueThen(toValueInfo());
    }

    public Function<ValueInfo, TypeResult<ValueInfo>> typeCheckBody(final FunctionWithBodyNode function) {
        return new Function<ValueInfo, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(ValueInfo returnTypeInfo) {
                List<? extends Type> functionTypeParameters = ((TypeApplication)returnTypeInfo.getType()).getTypeParameters();
                Type returnType = functionTypeParameters.get(functionTypeParameters.size() - 1);
                TypeResult<ValueInfo> result = success(returnTypeInfo);

                TypeResult<StatementTypeCheckResult> blockResult = typeCheckBlock(function.getBody(), some(returnType));
                result = result.withErrorsFrom(blockResult);
                
                if (!blockResult.get().hasReturned()) {
                    result = result.withErrorsFrom(TypeResult.<Type>failure(error(
                        function,
                        new MissingReturnStatementError()
                    )));
                }
                return result;
            }
        };
    }

    private TypeResult<ValueInfo> inferCallType(final CallNode expression) {
        TypeResult<Type> calledTypeResult = inferType(expression.getFunction());
        return calledTypeResult.ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type calledType) {
                if (!CoreTypes.isFunction(calledType)) {
                    return TypeResult.failure(error(expression, "Cannot call objects that aren't functions"));
                }
                TypeApplication functionType = (TypeApplication)calledType;
                final List<? extends Type> typeParameters = functionType.getTypeParameters();
                
                int numberOfFormalAguments = typeParameters.size() - 1;
                int numberOfActualArguments = expression.getArguments().size();
                if (numberOfFormalAguments != numberOfActualArguments) {
                    String errorMessage = "Function requires " + numberOfFormalAguments + " argument(s), but is called with " + numberOfActualArguments;
                    CompilerError error = CompilerErrors.error(expression, errorMessage);
                    return TypeResult.failure(asList(error));
                }
                Type returnType = typeParameters.get(numberOfFormalAguments);
                TypeResult<Type> result = success(returnType);
                for (int i = 0; i < numberOfFormalAguments; i++) {
                    final int index = i;
                    final ExpressionNode argument = expression.getArguments().get(i);
                    result = result.withErrorsFrom(inferType(argument).ifValueThen(new Function<Type, TypeResult<Void>>() {
                        @Override
                        public TypeResult<Void> apply(Type actualArgumentType) {
                            if (isSubType(actualArgumentType, typeParameters.get(index), context)) {
                                return success();
                            } else {
                                return failure(error(argument, ("Expected expression of type " + typeParameters.get(index).shortName() +
                                " as argument " + (index + 1) + ", but got expression of type " + actualArgumentType.shortName())));
                            }
                        }
                    }));
                }
                return result;
            }
        }).ifValueThen(toValueInfo());
    }

    private TypeResult<ValueInfo> inferMemberAccessType(final MemberAccessNode memberAccess) {
        return inferType(memberAccess.getExpression()).ifValueThen(new Function<Type, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(Type leftType) {
                String name = memberAccess.getMemberName();
                ScalarTypeInfo leftTypeInfo = context.getInfo((ScalarType)leftType);
                Map<String, ValueInfo> members = leftTypeInfo.getMembers();
                
                if (members.containsKey(name)) {
                    return TypeResult.success(members.get(name));
                } else {
                    return TypeResult.failure(error(memberAccess, ("No such member: " + name)));
                }
            }
        });
    }

    private TypeResult<ValueInfo> inferTypeApplicationType(final TypeApplicationNode typeApplication) {
        return inferType(typeApplication.getBaseValue()).ifValueThen(new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type baseType) {
                List<Type> parameterTypes = Lists.transform(typeApplication.getParameters(), toParameterType());
                
                if (baseType instanceof ParameterisedFunctionType) {
                    ParameterisedFunctionType functionType = (ParameterisedFunctionType)baseType;
                    ImmutableMap.Builder<FormalTypeParameter, Type> replacements = ImmutableMap.builder();
                    for (int i = 0; i < functionType.getFormalTypeParameters().size(); i += 1) {
                        replacements.put(functionType.getFormalTypeParameters().get(i), parameterTypes.get(i));
                    }
                    return TypeResult.success(CoreTypes.functionTypeOf(Eager.transform(functionType.getFunctionTypeParameters(), toReplacement(replacements.build()))));
                } else if (baseType instanceof ParameterisedType) {
                    return TypeResult.success((Type)CoreTypes.classOf(new TypeApplication((ParameterisedType)baseType, parameterTypes)));   
                } else {
                    throw new RuntimeException("Don't know how to apply types " + parameterTypes + " to " + baseType);
                }
            }
        }).ifValueThen(toValueInfo());
    }

    private Function<Type, Type> toReplacement(final ImmutableMap<FormalTypeParameter, Type> replacements) {
        return new Function<Type, Type>() {
            @Override
            public Type apply(Type input) {
                return new TypeReplacer().replaceTypes(input, replacements);
            }
        };
    }

    private TypeResult<ValueInfo> inferAssignmentType(AssignmentExpressionNode expression) {
        TypeResult<Type> valueTypeResult = inferType(expression.getValue());
        TypeResult<ValueInfo> targetInfo = inferValueInfo(expression.getTarget())
            .ifValueThen(checkIsAssignable(expression));
        
        TypeResult<ValueInfo> result = valueTypeResult.withErrorsFrom(targetInfo).ifValueThen(toValueInfo());
        if (valueTypeResult.hasValue() && targetInfo.hasValue()) {
            Type valueType = valueTypeResult.get();
            Type targetType = targetInfo.get().getType();
            if (!isSubType(valueType, targetType, context)) {
                result = result.withErrorsFrom(failure(error(expression, new TypeMismatchError(targetType, valueType))));
            }
        }
        return result;
    }
    
    private static Function<ValueInfo, TypeResult<ValueInfo>> checkIsAssignable(final AssignmentExpressionNode expression) {
        return new Function<ValueInfo, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(ValueInfo input) {
                if (input.isAssignable()) {
                    return TypeResult.success(input);
                } else {
                    return TypeResult.failure(input, error(expression, new InvalidAssignmentError()));
                }
            }
        };
    }

    private Function<ExpressionNode, Type> toParameterType() {
        return new Function<ExpressionNode, Type>() {
            @Override
            public Type apply(ExpressionNode expression) {
                TypeResult<Type> result = typeLookup.lookupTypeReference(expression);
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
                return success(CoreTypes.functionTypeOf(typeParameters));
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

    private TypeResult<StatementTypeCheckResult> typeCheckBlock(BlockNode block, Option<Type> returnType) {
        return blockTypeChecker.forwardDeclareAndTypeCheck(block, returnType);
    }

}
