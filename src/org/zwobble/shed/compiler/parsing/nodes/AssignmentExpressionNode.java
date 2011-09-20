package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class AssignmentExpressionNode implements ExpressionNode {
    private final ExpressionNode target;
    private final ExpressionNode value;
}
