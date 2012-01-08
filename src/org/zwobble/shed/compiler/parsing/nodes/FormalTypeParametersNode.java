package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Iterator;
import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class FormalTypeParametersNode implements SyntaxNode, Iterable<FormalTypeParameterNode> {
    private final List<FormalTypeParameterNode> formalTypeParameters;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(formalTypeParameters));
    }
    
    @Override
    public Iterator<FormalTypeParameterNode> iterator() {
        return formalTypeParameters.iterator();
    }
}
