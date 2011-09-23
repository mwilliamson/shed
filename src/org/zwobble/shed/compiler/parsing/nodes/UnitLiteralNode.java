package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;

import lombok.Data;

@Data
public class UnitLiteralNode implements LiteralNode {
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(Collections.<SyntaxNode>emptyList());
    }
}
