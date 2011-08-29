package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.Option;

import lombok.Data;

@Data
public class ImmutableVariableNode implements VariableDeclarationNode {
    private final String identifier;
    private final Option<? extends ExpressionNode> typeReference;
    private final ExpressionNode value;
}
