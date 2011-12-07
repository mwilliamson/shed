package org.zwobble.shed.compiler.typechecker;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.zwobble.shed.compiler.Eager;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.parsing.nodes.AssignmentExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.UnitLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.expressions.AssignmentExpressionTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.CallExpressionTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.ExpressionTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.LiteralExpressionTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.LongLambdaExpressionTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.ShortLambdaExpressionTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.VariableLookup;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.Member;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeReplacer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.Results.isSuccess;
import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfos.toUnassignableValueInfo;
import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;

public class TypeInfererImpl implements TypeInferer {
    private final TypeLookup typeLookup;
    private final MetaClasses metaClasses;
    private final StaticContext context;
    
    private final Map<Class<? extends ExpressionNode>, Provider<ExpressionTypeInferer<? extends ExpressionNode>>> typeInferers = Maps.newHashMap();
    private final Injector injector;

    @Inject
    public TypeInfererImpl(
        TypeLookup typeLookup, 
        MetaClasses metaClasses,
        StaticContext context,
        Injector injector
    ) {
        this.typeLookup = typeLookup;
        this.metaClasses = metaClasses;
        this.context = context;
        this.injector = injector;

        putTypeInferer(BooleanLiteralNode.class, new LiteralExpressionTypeInferer<BooleanLiteralNode>(CoreTypes.BOOLEAN));
        putTypeInferer(NumberLiteralNode.class, new LiteralExpressionTypeInferer<NumberLiteralNode>(CoreTypes.DOUBLE));
        putTypeInferer(StringLiteralNode.class, new LiteralExpressionTypeInferer<StringLiteralNode>(CoreTypes.STRING));
        putTypeInferer(UnitLiteralNode.class, new LiteralExpressionTypeInferer<UnitLiteralNode>(CoreTypes.UNIT));
        putTypeInferer(VariableIdentifierNode.class, VariableLookup.class);
        putTypeInferer(AssignmentExpressionNode.class, AssignmentExpressionTypeInferer.class);
        putTypeInferer(ShortLambdaExpressionNode.class, ShortLambdaExpressionTypeInferer.class);
        putTypeInferer(LongLambdaExpressionNode.class, LongLambdaExpressionTypeInferer.class);
        putTypeInferer(CallNode.class, CallExpressionTypeInferer.class);
    }
    
    private <T extends ExpressionNode> void putTypeInferer(Class<T> expressionType, final ExpressionTypeInferer<T> typeInferer) {
        typeInferers.put(expressionType, new Provider<ExpressionTypeInferer<? extends ExpressionNode>>() {
            @Override
            public ExpressionTypeInferer<? extends ExpressionNode> get() {
                return typeInferer;
            }
        });
    }
    
    private <T extends ExpressionNode> void putTypeInferer(Class<T> expressionType, final Class<? extends ExpressionTypeInferer<T>> typeInfererType) {
        typeInferers.put(expressionType, new Provider<ExpressionTypeInferer<? extends ExpressionNode>>() {
            @Override
            public ExpressionTypeInferer<? extends ExpressionNode> get() {
                return injector.getInstance(typeInfererType);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends ExpressionNode> ExpressionTypeInferer<T> getTypeInferer(T expression) {
        return (ExpressionTypeInferer<T>) typeInferers.get(expression.getClass()).get();
    }
    
    public TypeResult<ValueInfo> inferValueInfo(ExpressionNode expression) {
        if (typeInferers.containsKey(expression.getClass())) {
            return getTypeInferer(expression).inferValueInfo(expression);
        }
        if (expression instanceof MemberAccessNode) {
            return inferMemberAccessType((MemberAccessNode)expression);
        }
        if (expression instanceof TypeApplicationNode) {
            return inferTypeApplicationType((TypeApplicationNode)expression);
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

    private TypeResult<ValueInfo> inferMemberAccessType(final MemberAccessNode memberAccess) {
        return inferType(memberAccess.getExpression()).ifValueThen(new Function<Type, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(Type leftType) {
                String name = memberAccess.getMemberName();
                ScalarTypeInfo leftTypeInfo = context.getInfo((ScalarType)leftType);
                Members members = leftTypeInfo.getMembers();
                Option<Member> member = members.lookup(name);
                
                if (member.hasValue()) {
                    return success(member.get().getValueInfo());
                } else {
                    return failure(error(memberAccess, ("No such member: " + name)));
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
                    return success((Type)CoreTypes.functionTypeOf(Eager.transform(functionType.getFunctionTypeParameters(), toReplacement(replacements.build()))));
                } else if (baseType instanceof ParameterisedType) {
                    return success((Type)metaClasses.metaClassOf(applyTypes((ParameterisedType)baseType, parameterTypes)));   
                } else {
                    throw new RuntimeException("Don't know how to apply types " + parameterTypes + " to " + baseType);
                }
            }
        }).ifValueThen(toUnassignableValueInfo());
    }

    private Function<Type, Type> toReplacement(final ImmutableMap<FormalTypeParameter, Type> replacements) {
        return new Function<Type, Type>() {
            @Override
            public Type apply(Type input) {
                return new TypeReplacer().replaceTypes(input, replacements);
            }
        };
    }

    private Function<ExpressionNode, Type> toParameterType() {
        return new Function<ExpressionNode, Type>() {
            @Override
            public Type apply(ExpressionNode expression) {
                TypeResultWithValue<Type> result = typeLookup.lookupTypeReference(expression);
                if (!isSuccess(result)) {
                    // TODO: handle failure
                    throw new RuntimeException(result.getErrors().toString());
                }
                return result.get();
            }
        };
    }

}
