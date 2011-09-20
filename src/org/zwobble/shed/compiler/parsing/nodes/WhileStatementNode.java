package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class WhileStatementNode implements StatementNode {
    private final ExpressionNode condition;
    private final BlockNode body;
}
