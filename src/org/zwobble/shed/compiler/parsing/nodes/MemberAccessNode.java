package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class MemberAccessNode implements ExpressionNode {
    private final ExpressionNode expression;
    private final String memberName;
}
