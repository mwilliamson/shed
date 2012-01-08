package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.subScope;

@Data
public class FunctionDeclarationNode implements DeclarationNode, FunctionWithBodyNode, HoistableStatementNode {
    private final String identifier;
    private final Option<FormalTypeParametersNode> formalTypeParameters;
    private final List<FormalArgumentNode> formalArguments;
    private final ExpressionNode returnType;
    private final BlockNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(
            sameScope(asList(returnType)),
            subScope(concat(formalTypeParameters, formalArguments, asList(body))
        ));
    }
}
