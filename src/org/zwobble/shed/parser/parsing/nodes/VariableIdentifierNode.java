package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class VariableIdentifierNode implements ExpressionNode {
    private final String identifier;
}
