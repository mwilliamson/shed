package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import org.zwobble.shed.compiler.Option;

import lombok.Data;

@Data
public class ShortLambdaExpressionNode implements LambdaExpressionNode {
    private final List<FormalArgumentNode> formalArguments;
    private final Option<? extends ExpressionNode> returnType;
    private final ExpressionNode body;
}
