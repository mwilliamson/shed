package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singletonList;

import lombok.Data;

@Data
public class CallNode implements ExpressionNode {
    private final ExpressionNode function;
    private final List<ExpressionNode> arguments;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(concat(singletonList((function)), arguments));
    }
}
