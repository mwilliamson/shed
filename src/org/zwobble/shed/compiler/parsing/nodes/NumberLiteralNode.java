package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class NumberLiteralNode implements ExpressionNode {
    private final String value;
}
