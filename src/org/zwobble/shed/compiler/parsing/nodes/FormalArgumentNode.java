package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static java.util.Collections.singletonList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class FormalArgumentNode implements DeclarationNode {
    private final String identifier;
    private final ExpressionNode type;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(singletonList(type)));
    }
}
