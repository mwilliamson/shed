package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import static java.util.Arrays.asList;

@Data
public class IfThenElseStatementNode implements StatementNode {
    private final ExpressionNode condition;
    private final BlockNode ifTrue;
    private final BlockNode ifFalse;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(asList(condition, ifTrue, ifFalse));
    }
}
