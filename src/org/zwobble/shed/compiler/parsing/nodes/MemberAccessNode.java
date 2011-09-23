package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import static java.util.Collections.singletonList;

@Data
public class MemberAccessNode implements ExpressionNode {
    private final ExpressionNode expression;
    private final String memberName;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(singletonList(expression));
    }
}
