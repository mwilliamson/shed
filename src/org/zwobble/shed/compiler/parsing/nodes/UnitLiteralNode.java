package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class UnitLiteralNode implements LiteralNode {
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.LEAF;
    }
}
