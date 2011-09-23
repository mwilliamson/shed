package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class VariableIdentifierNode implements ExpressionNode {
    private final String identifier;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.LEAF;
    }
}
