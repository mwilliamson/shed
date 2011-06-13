package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class StringLiteralNode implements ExpressionNode {
    private final String value;
}
