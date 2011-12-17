package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Iterator;

import lombok.AllArgsConstructor;

import org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes;
import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;
import org.zwobble.shed.compiler.util.Eager;

import com.google.common.base.Function;

import static java.util.Arrays.asList;

@AllArgsConstructor
public class EntireSourceNode implements SyntaxNode, Iterable<SourceNode> {
    private final Iterable<SourceNode> sourceNodes;
    
    @Override
    public Iterator<SourceNode> iterator() {
        return sourceNodes.iterator();
    }
    
    @Override
    public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(Eager.transform(sourceNodes, toSubScope()));
    }
    
    private Function<SourceNode, ScopedNodes> toSubScope() {
        return new Function<SourceNode, ScopedNodes>() {
            @Override
            public ScopedNodes apply(SourceNode input) {
                return ScopedNodes.subScope(asList(input));
            }
        };
    }
}
