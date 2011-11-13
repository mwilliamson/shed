package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Iterator;
import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class InterfaceBodyNode implements SyntaxNode, Iterable<FunctionSignatureDeclarationNode> {
    private final List<FunctionSignatureDeclarationNode> statements;
    
    @Override
    public Iterator<FunctionSignatureDeclarationNode> iterator() {
        return statements.iterator();
    }
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(statements));
    }
}
