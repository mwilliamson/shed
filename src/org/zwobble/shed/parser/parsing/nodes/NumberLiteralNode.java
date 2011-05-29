package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class NumberLiteralNode implements ExpressionNode {
    private final String value;
}
