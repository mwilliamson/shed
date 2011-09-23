package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class BooleanLiteralNode implements LiteralNode {
    private final boolean value;
    
    public boolean getValue() {
        return value;
    }
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(Collections.<SyntaxNode>emptyList());
    }
}
