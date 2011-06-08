package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class ReturnNode implements StatementNode {
    private final ExpressionNode expression;
}
