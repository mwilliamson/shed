package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import lombok.Data;

@Data
public class ImportNode implements DeclarationNode {
    private final List<String> names;
    
    @Override
    public String getIdentifier() {
        return names.get(names.size() - 1);
    }
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.LEAF;
    }
}
