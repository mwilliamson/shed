package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static java.util.Collections.singletonList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class PublicDeclarationNode implements StatementNode {
    private final DeclarationNode declaration;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(singletonList(declaration)));
    }
}
