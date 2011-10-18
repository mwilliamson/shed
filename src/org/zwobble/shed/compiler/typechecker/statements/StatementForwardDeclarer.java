package org.zwobble.shed.compiler.typechecker.statements;

import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;

public interface StatementForwardDeclarer<T extends StatementNode> {
    TypeResult<?> forwardDeclare(T statement);
}
