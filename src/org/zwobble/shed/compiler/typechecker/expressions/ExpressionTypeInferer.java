package org.zwobble.shed.compiler.typechecker.expressions;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;

public interface ExpressionTypeInferer<T extends ExpressionNode> {
    TypeResult<ValueInfo> inferValueInfo(T expression);
}
