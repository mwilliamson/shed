package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

public interface StatementForwardDeclarer<T extends StatementNode> {
    TypeResult<?> forwardDeclare(T statement, StaticContext context);
}
