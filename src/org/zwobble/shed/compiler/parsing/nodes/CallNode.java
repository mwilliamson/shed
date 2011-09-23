package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singletonList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class CallNode implements ExpressionNode {
    private final ExpressionNode function;
    private final List<ExpressionNode> arguments;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(concat(singletonList((function)), arguments)));
    }
}
