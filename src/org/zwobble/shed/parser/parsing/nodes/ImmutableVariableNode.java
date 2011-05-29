package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class ImmutableVariableNode {
    private final String identifier;
    private final ExpressionNode value;
}
