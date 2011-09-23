package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.subScope;

@Data
public class LongLambdaExpressionNode implements LambdaExpressionNode, FunctionWithBodyNode {
    private final List<FormalArgumentNode> formalArguments;
    private final ExpressionNode returnType;
    private final BlockNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        // TODO: test that returnType cannot be a reference to an argument i.e. they're in distinct scopes
        return SyntaxNodeStructure.build(
            sameScope(asList(returnType)),
            subScope(concat(formalArguments, asList(body)))
       );
    }
}
