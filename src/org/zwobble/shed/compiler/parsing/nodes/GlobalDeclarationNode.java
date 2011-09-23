package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;

import lombok.Data;

@Data
public class GlobalDeclarationNode implements DeclarationNode {
    private final String identifier;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(Collections.<SyntaxNode>emptyList());
    }
}
