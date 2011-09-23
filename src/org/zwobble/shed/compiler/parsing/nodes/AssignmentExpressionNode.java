package org.zwobble.shed.compiler.parsing.nodes;

import static java.util.Arrays.asList;

import lombok.Data;

@Data
public class AssignmentExpressionNode implements ExpressionNode {
    private final ExpressionNode target;
    private final ExpressionNode value;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(asList(target, value));
    }
}
