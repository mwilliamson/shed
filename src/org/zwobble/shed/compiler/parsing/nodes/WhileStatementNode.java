package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import static java.util.Arrays.asList;

@Data
public class WhileStatementNode implements StatementNode {
    private final ExpressionNode condition;
    private final BlockNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(asList(condition, body));
    }
}
