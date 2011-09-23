package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import org.zwobble.shed.compiler.Option;

import static java.util.Arrays.asList;

@Data
public class VariableDeclarationNode implements DeclarationNode {
    public static VariableDeclarationNode immutable(String identifier, Option<? extends ExpressionNode> type, ExpressionNode value) {
        return new VariableDeclarationNode(identifier, type, value, false);
    }
    
    public static VariableDeclarationNode mutable(String identifier, Option<? extends ExpressionNode> type, ExpressionNode value) {
        return new VariableDeclarationNode(identifier, type, value, true);
    }
    
    private final String identifier;
    private final Option<? extends ExpressionNode> typeReference;
    private final ExpressionNode value;
    private final boolean isMutable;

    @Override
    public SyntaxNodeStructure describeStructure() {
        if (typeReference.hasValue()) {
            return SyntaxNodeStructure.build(asList(value, typeReference.get()));
        } else {
            return SyntaxNodeStructure.build(asList(value));
        }
    }
}
