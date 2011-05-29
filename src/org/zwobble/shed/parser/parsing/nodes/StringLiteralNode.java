package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class StringLiteralNode implements ExpressionNode {
    private final String value;
}
