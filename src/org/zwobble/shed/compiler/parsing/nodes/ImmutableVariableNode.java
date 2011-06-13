package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.Option;

import lombok.Data;

@Data
public class ImmutableVariableNode implements StatementNode {
    private final String identifier;
    private final Option<? extends TypeReferenceNode> typeReference;
    private final ExpressionNode value;
}
