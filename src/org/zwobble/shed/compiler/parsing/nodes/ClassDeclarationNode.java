package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;

import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.subScope;

@Data
public class ClassDeclarationNode implements TypeDeclarationNode, HoistableStatementNode {
    private final String identifier;
    private final List<FormalArgumentNode> formalArguments;
    private final BlockNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(subScope(concat(formalArguments, asList(body))));
    }
}
