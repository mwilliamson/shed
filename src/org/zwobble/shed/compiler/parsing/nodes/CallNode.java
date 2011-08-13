package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class CallNode implements ExpressionNode {
    private final ExpressionNode function;
    private final List<ExpressionNode> arguments;
}
