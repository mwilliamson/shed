package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class LongLambdaExpressionNode implements LambdaExpressionNode {
    private final List<FormalArgumentNode> formalArguments;
    private final TypeReferenceNode returnType;
    private final List<StatementNode> body;
}
