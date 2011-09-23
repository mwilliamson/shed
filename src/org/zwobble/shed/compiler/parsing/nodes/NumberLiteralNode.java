package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import lombok.Data;

@Data
public class NumberLiteralNode implements LiteralNode {
    private final String value;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.LEAF;
    }
}
