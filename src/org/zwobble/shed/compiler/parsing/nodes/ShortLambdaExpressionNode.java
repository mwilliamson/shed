package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;
import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.Option;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singletonList;

@Data
public class ShortLambdaExpressionNode implements LambdaExpressionNode {
    private final List<FormalArgumentNode> formalArguments;
    private final Option<? extends ExpressionNode> returnType;
    private final ExpressionNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        Iterable<? extends ExpressionNode> typeNodes = returnType.hasValue() 
            ? singletonList(returnType.get()) 
            : Collections.<ExpressionNode>emptyList();
        return SyntaxNodeStructure.build(concat(formalArguments, typeNodes, singletonList(body)));
    }
}
