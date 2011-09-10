package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.Node;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNodeIdentifier;

public class SimpleNodeLocations implements NodeLocations {
    private final Map<SyntaxNodeIdentifier, SourceRange> locations = new HashMap<SyntaxNodeIdentifier, SourceRange>();
    
    public void put(SyntaxNode node, SourceRange location) {
        locations.put(new SyntaxNodeIdentifier(node), location);
    }
    
    @Override
    public SourceRange locate(Node node) {
        return locations.get(new SyntaxNodeIdentifier(node));
    }
}
