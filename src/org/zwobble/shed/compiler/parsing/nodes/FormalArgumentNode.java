package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import static java.util.Collections.singletonList;

@Data
public class FormalArgumentNode implements DeclarationNode {
    private final String identifier;
    private final ExpressionNode type;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(singletonList(type));
    }
}
