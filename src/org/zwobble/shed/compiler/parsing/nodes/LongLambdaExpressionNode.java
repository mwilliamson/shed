package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;

import lombok.Data;

@Data
public class LongLambdaExpressionNode implements LambdaExpressionNode, FunctionWithBodyNode {
    private final List<FormalArgumentNode> formalArguments;
    private final ExpressionNode returnType;
    private final BlockNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(concat(formalArguments, asList(returnType, body)));
    }
}
