package org.zwobble.shed.compiler.typechecker.statements;

import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;

public class NoOpForwardDeclarer<T extends StatementNode> implements StatementForwardDeclarer<T> {
    @Override
    public TypeResult<Void> forwardDeclare(StatementNode statement) {
        return TypeResult.success();
    }

}
