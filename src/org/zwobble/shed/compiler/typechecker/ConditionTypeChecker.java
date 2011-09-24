package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.typechecker.errors.ConditionNotBooleanError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

public class ConditionTypeChecker {
    public TypeResult<Void> typeAndCheckCondition(
        ExpressionNode condition, NodeLocations nodeLocations, StaticContext context
    ) {
        TypeResult<Type> conditionType = TypeInferer.inferType(condition, nodeLocations, context);
        return conditionType.ifValueThen(checkIsBoolean(nodeLocations.locate(condition)));
    }

    private static Function<Type, TypeResult<Void>> checkIsBoolean(final SourceRange conditionLocation) {
        return new Function<Type, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(Type input) {
                if (SubTyping.isSubType(input, CoreTypes.BOOLEAN)) {
                    return TypeResult.success();
                } else {
                    return TypeResult.failure(new CompilerError(
                        conditionLocation,
                        new ConditionNotBooleanError(input)
                    ));
                }
            }
        };
    }
}
