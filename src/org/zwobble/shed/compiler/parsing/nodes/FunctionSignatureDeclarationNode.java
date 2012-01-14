package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.subScope;

@Data
public class FunctionSignatureDeclarationNode implements DeclarationNode, FunctionNode {
    private final String identifier;
    private final List<FormalArgumentNode> formalArguments;
    private final ExpressionNode returnType;

    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(
            sameScope(asList(returnType)),
            subScope(formalArguments)
        );
    }
    
    @Override
    public Option<FormalTypeParametersNode> getFormalTypeParameters() {
        return Option.none();
    }
}
