package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class ExpressionStatementNode implements StatementNode {
    private final ExpressionNode expression;
}
