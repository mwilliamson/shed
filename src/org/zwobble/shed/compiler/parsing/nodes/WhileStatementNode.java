package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class WhileStatementNode implements StatementNode {
    private final ExpressionNode condition;
    private final BlockNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(asList(condition, body)));
    }
}
