package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

public class NoOpForwardDeclarer<T extends StatementNode> implements StatementForwardDeclarer<T> {
    @Override
    public TypeResult<Void> forwardDeclare(StatementNode statement, NodeLocations nodeLocations, StaticContext context) {
        return TypeResult.success();
    }

}