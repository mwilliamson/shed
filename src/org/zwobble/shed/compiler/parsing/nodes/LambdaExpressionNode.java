package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

public interface LambdaExpressionNode extends ExpressionNode {
    List<FormalArgumentNode> getFormalArguments();
    SyntaxNode getBody();
}
