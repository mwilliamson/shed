package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.subScope;

@Data
public class InterfaceDeclarationNode implements TypeDeclarationNode {
    private final String identifier;
    private final Option<FormalTypeParametersNode> formalTypeParameters;
    private final InterfaceBodyNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(subScope(concat(formalTypeParameters, asList(body))));
    }
}
