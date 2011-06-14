package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class LongLambdaExpressionNode implements ExpressionNode {
    private final List<FormalArgumentNode> arguments;
    private final TypeReferenceNode returnType;
    private final List<StatementNode> body;
}
