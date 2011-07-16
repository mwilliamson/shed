package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.Option;

public interface VariableDeclarationNode extends StatementNode {
    String getIdentifier();
    Option<? extends TypeReferenceNode> getTypeReference();
    ExpressionNode getValue();
}
