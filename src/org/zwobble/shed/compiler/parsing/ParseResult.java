package org.zwobble.shed.compiler.parsing;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNodeIdentifier;

@RequiredArgsConstructor
public class ParseResult<T extends SyntaxNode> {
    private final T node;
    private final Map<SyntaxNodeIdentifier, SourceRange> nodePositions;
    
    public T getNode() {
        return node;
    }
    
    public SourceRange positionOf(SourceNode node) {
        return nodePositions.get(new SyntaxNodeIdentifier(node));
    }
}
