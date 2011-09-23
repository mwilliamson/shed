package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

public interface FunctionWithBodyNode extends SyntaxNode {
    List<FormalArgumentNode> getFormalArguments();
    ExpressionNode getReturnType();
    BlockNode getBody();
}
