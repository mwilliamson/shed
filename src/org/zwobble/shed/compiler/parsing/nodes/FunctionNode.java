package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

public interface FunctionNode extends SyntaxNode {
    List<FormalArgumentNode> getFormalArguments();
    ExpressionNode getReturnType();

}
