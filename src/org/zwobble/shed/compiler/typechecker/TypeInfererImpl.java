package org.zwobble.shed.compiler.typechecker;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

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
import org.zwobble.shed.compiler.typechecker.expressions.MemberAccessTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.ShortLambdaExpressionTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.TypeApplicationTypeInferer;
import org.zwobble.shed.compiler.typechecker.expressions.VariableLookup;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

public class TypeInfererImpl implements TypeInferer {
    private final Map<Class<? extends ExpressionNode>, Provider<ExpressionTypeInferer<? extends ExpressionNode>>> typeInferers = Maps.newHashMap();
    private final Injector injector;

    @Inject
    public TypeInfererImpl(
        Injector injector
    ) {
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
        putTypeInferer(MemberAccessNode.class, MemberAccessTypeInferer.class);
        putTypeInferer(TypeApplicationNode.class, TypeApplicationTypeInferer.class);
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

}
