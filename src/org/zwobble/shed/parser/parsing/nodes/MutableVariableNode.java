package org.zwobble.shed.parser.parsing.nodes;

import org.zwobble.shed.parser.Option;

import lombok.Data;

@Data
public class MutableVariableNode implements StatementNode {
    private final String identifier;
    private final Option<? extends TypeReferenceNode> typeReference;
    private final ExpressionNode value;
}
