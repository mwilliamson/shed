package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static java.util.Arrays.asList;

import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;

@Data
public class AssignmentExpressionNode implements ExpressionNode {
    private final ExpressionNode target;
    private final ExpressionNode value;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(sameScope(asList(target, value)));
    }
}
