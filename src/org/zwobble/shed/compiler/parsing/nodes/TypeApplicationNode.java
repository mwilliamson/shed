package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class TypeApplicationNode implements ExpressionNode {
    private final ExpressionNode baseValue;
    private final List<ExpressionNode> parameters;
}
