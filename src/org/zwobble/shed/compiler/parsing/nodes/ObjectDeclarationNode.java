package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import static java.util.Collections.singletonList;

@Data
public class ObjectDeclarationNode implements TypeDeclarationNode {
    private final String identifier;
    private final BlockNode statements;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(singletonList(statements));
    }
}
