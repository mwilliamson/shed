package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import org.zwobble.shed.compiler.Option;

import lombok.Data;

@Data
public class ShortLambdaExpressionNode implements ExpressionNode {
    private final List<FormalArgumentNode> arguments;
    private final Option<TypeReferenceNode> returnType;
    private final ExpressionNode body;
}
