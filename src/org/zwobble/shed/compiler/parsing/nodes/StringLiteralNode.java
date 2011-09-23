package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import lombok.Data;

@Data
public class StringLiteralNode implements LiteralNode {
    private final String value;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.LEAF;
    }
}
