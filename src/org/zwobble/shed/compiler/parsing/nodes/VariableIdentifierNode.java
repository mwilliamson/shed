package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import lombok.Data;

@Data
public class VariableIdentifierNode implements ExpressionNode {
    private final String identifier;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.LEAF;
    }
}
