package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;

import lombok.Data;

@Data
public class VariableIdentifierNode implements ExpressionNode {
    private final String identifier;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(Collections.<SyntaxNode>emptyList());
    }
}
