package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import static java.util.Collections.singletonList;

@Data
public class ReturnNode implements StatementNode {
    private final ExpressionNode expression;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(singletonList(expression));
    }
}
