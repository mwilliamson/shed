package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class IfThenElseStatementNode implements StatementNode {
    private final ExpressionNode condition;
    private final List<StatementNode> ifTrue;
    private final List<StatementNode> ifFalse;
}
