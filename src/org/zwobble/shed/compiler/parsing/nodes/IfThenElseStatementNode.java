package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class IfThenElseStatementNode implements StatementNode {
    private final ExpressionNode condition;
    private final BlockNode ifTrue;
    private final BlockNode ifFalse;
}
