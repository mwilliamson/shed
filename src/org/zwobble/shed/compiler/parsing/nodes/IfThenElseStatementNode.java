package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class IfThenElseStatementNode implements StatementNode {
    private final ExpressionNode condition;
    private final BlockNode ifTrue;
    private final BlockNode ifFalse;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(asList(condition, ifTrue, ifFalse)));
    }
}
