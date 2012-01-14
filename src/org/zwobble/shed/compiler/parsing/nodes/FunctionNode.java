package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import org.zwobble.shed.compiler.Option;

public interface FunctionNode extends SyntaxNode {
    Option<FormalTypeParametersNode> getFormalTypeParameters();
    List<FormalArgumentNode> getFormalArguments();
    ExpressionNode getReturnType();

}
