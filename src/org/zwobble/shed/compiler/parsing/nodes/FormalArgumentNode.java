package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class FormalArgumentNode implements SyntaxNode {
    private final String name;
    private final ExpressionNode type;
}
