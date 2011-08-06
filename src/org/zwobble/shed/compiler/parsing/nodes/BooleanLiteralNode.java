package org.zwobble.shed.compiler.parsing.nodes;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class BooleanLiteralNode implements ExpressionNode {
    private final boolean value;
    
    public boolean getValue() {
        return value;
    }
}
