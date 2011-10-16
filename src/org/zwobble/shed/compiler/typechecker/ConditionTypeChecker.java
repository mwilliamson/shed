package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.typechecker.errors.ConditionNotBooleanError;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

public class ConditionTypeChecker {
    private final NodeLocations nodeLocations;
    private final TypeInferer typeInferer;

    @Inject
    public ConditionTypeChecker(TypeInferer typeInferer, NodeLocations nodeLocations) {
        this.typeInferer = typeInferer;
        this.nodeLocations = nodeLocations;
    }
    
    public TypeResult<Void> typeAndCheckCondition(
        ExpressionNode condition, StaticContext context
    ) {
        TypeResult<Type> conditionType = typeInferer.inferType(condition, context);
        return conditionType.ifValueThen(checkIsBoolean(nodeLocations.locate(condition), context));
    }

    private static Function<Type, TypeResult<Void>> checkIsBoolean(final SourceRange conditionLocation, final StaticContext context) {
        return new Function<Type, TypeResult<Void>>() {
            @Override
            public TypeResult<Void> apply(Type input) {
                if (SubTyping.isSubType(input, CoreTypes.BOOLEAN, context)) {
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
