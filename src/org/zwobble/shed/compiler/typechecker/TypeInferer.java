package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.Type;

public interface TypeInferer {
    TypeResult<Type> inferType(ExpressionNode condition);
    TypeResult<ValueInfo> inferValueInfo(ExpressionNode expression);
}
