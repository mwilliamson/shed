package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import static java.util.Collections.singletonList;

@Data
public class ExpressionStatementNode implements StatementNode {
    private final ExpressionNode expression;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(singletonList(expression));
    }
}
