package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class ReturnNode implements StatementNode {
    private final ExpressionNode expression;
}
