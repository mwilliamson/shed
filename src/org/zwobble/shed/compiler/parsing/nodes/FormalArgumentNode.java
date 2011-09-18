package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class FormalArgumentNode implements DeclarationNode {
    private final String identifier;
    private final ExpressionNode type;
}
