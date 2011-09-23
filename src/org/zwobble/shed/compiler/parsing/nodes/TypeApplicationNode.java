package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;
import java.util.List;

import lombok.Data;

import static com.google.common.collect.Iterables.concat;

@Data
public class TypeApplicationNode implements ExpressionNode {
    private final ExpressionNode baseValue;
    private final List<ExpressionNode> parameters;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(concat(Collections.singletonList(baseValue), parameters));
    }
}
