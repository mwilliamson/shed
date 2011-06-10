package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class BooleanLiteralNode implements ExpressionNode {
    private final boolean value;
}
