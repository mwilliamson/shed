package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.Option;

import static java.util.Arrays.asList;

import lombok.Data;

@Data
public class MutableVariableNode implements VariableDeclarationNode {
    private final String identifier;
    private final Option<? extends ExpressionNode> typeReference;
    private final ExpressionNode value;

    @Override
    public SyntaxNodeStructure describeStructure() {
        if (typeReference.hasValue()) {
            return SyntaxNodeStructure.build(asList(value, typeReference.get()));
        } else {
            return SyntaxNodeStructure.build(asList(value));
        }
    }
}
