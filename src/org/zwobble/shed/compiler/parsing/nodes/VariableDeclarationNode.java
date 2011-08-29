package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.Option;

public interface VariableDeclarationNode extends DeclarationNode {
    String getIdentifier();
    Option<? extends ExpressionNode> getTypeReference();
    ExpressionNode getValue();
}
