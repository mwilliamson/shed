package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.Node;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

public class SimpleNodeLocations implements NodeLocations {
    private final Map<Identity<?>, SourceRange> locations = new HashMap<Identity<?>, SourceRange>();
    
    public void put(SyntaxNode node, SourceRange location) {
        locations.put(new Identity<SyntaxNode>(node), location);
    }
    
    @Override
    public SourceRange locate(Node node) {
        return locations.get(new Identity<Node>(node));
    }
}
