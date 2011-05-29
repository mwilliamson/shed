package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class MutableVariableNode {
    private final String identifier;
    private final ExpressionNode value;
}
