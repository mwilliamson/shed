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
    private final StaticContext context;

    @Inject
    public ConditionTypeChecker(TypeInferer typeInferer, NodeLocations nodeLocations, StaticContext context) {
        this.typeInferer = typeInferer;
        this.nodeLocations = nodeLocations;
        this.context = context;
    }
    
    public TypeResult<Void> typeAndCheckCondition(ExpressionNode condition) {
        TypeResult<Type> conditionType = typeInferer.inferType(condition);
        return conditionType.ifValueThen(checkIsBoolean(nodeLocations.locate(condition)));
    }

    private Function<Type, TypeResult<Void>> checkIsBoolean(final SourceRange conditionLocation) {
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
