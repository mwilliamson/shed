package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import lombok.Data;

@Data
public class PackageDeclarationNode implements SyntaxNode {
    private final List<String> packageNames;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.LEAF;
    }
}
