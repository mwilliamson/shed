package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;
import java.util.List;

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
        return SyntaxNodeStructure.build(Collections.<SyntaxNode>emptyList());
    }
}
