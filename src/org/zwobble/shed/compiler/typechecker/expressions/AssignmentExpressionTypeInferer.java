package org.zwobble.shed.compiler.typechecker.expressions;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.AssignmentExpressionNode;
import org.zwobble.shed.compiler.typechecker.SubTyping;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.InvalidAssignmentError;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfos.toUnassignableValueInfo;

public class AssignmentExpressionTypeInferer implements ExpressionTypeInferer<AssignmentExpressionNode> {
    private final TypeInferer typeInferer;
    private final SubTyping subTyping;

    @Inject
    public AssignmentExpressionTypeInferer(TypeInferer typeInferer, SubTyping subTyping) {
        this.typeInferer = typeInferer;
        this.subTyping = subTyping;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(AssignmentExpressionNode expression) {
        TypeResult<Type> valueTypeResult = typeInferer.inferType(expression.getValue());
        TypeResult<ValueInfo> targetInfo = typeInferer.inferValueInfo(expression.getTarget())
            .ifValueThen(checkIsAssignable(expression));
        
        TypeResult<ValueInfo> result = valueTypeResult.withErrorsFrom(targetInfo).ifValueThen(toUnassignableValueInfo());
        if (valueTypeResult.hasValue() && targetInfo.hasValue()) {
            Type valueType = valueTypeResult.getOrThrow();
            Type targetType = targetInfo.getOrThrow().getType();
            if (!subTyping.isSubType(valueType, targetType)) {
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
                    return success(input);
                } else {
                    return failure(input, error(expression, new InvalidAssignmentError()));
                }
            }
        };
    }
}
