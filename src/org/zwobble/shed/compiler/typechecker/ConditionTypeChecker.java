package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.typechecker.errors.ConditionNotBooleanError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;

import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

import static org.zwobble.shed.compiler.CompilerErrors.error;

public class ConditionTypeChecker {
    private final TypeInferer typeInferer;
    private final SubTyping subTyping;
    
    @Inject
    public ConditionTypeChecker(TypeInferer typeInferer, SubTyping subTyping) {
        this.typeInferer = typeInferer;
        this.subTyping = subTyping;
    }
    
    public TypeResult<Void> typeAndCheckCondition(ExpressionNode condition) {
        TypeResult<Type> conditionType = typeInferer.inferType(condition);
        return conditionType.ifValueThen(checkIsBoolean(condition));
    }

    private Function<Type, TypeResult<Void>> checkIsBoolean(final ExpressionNode condition) {
        return new Function<Type, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(Type input) {
                if (subTyping.isSubType(input, CoreTypes.BOOLEAN)) {
                    return success();
                } else {
                    return failure(error(condition, new ConditionNotBooleanError(input)));
                }
            }
        };
    }
}
