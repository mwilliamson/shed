package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;
import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singletonList;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.sameScope;
import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.subScope;

@Data
public class ShortLambdaExpressionNode implements LambdaExpressionNode {
    private final List<FormalArgumentNode> formalArguments;
    private final Option<? extends ExpressionNode> returnType;
    private final ExpressionNode body;
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        Iterable<? extends ExpressionNode> typeNodes = returnType.hasValue() 
            ? singletonList(returnType.get()) 
            : Collections.<ExpressionNode>emptyList();
        return SyntaxNodeStructure.build(
            sameScope(typeNodes),
            subScope(concat(formalArguments, singletonList(body)))
        );
    }
}
