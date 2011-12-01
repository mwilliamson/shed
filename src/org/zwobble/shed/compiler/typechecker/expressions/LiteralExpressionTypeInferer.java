package org.zwobble.shed.compiler.typechecker.expressions;

import org.zwobble.shed.compiler.parsing.nodes.LiteralNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResults.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class LiteralExpressionTypeInferer<T extends LiteralNode> implements ExpressionTypeInferer<T> {
    private final Type literalType;

    public LiteralExpressionTypeInferer(Type literalType) {
        this.literalType = literalType;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(T expression) {
        return success(unassignableValue(literalType));
    }
    
}
