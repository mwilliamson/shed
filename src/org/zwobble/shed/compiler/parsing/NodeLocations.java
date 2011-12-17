package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.Node;
import org.zwobble.shed.compiler.util.Pair;

public interface NodeLocations {
    SourceRange locate(Node node);
    Iterable<Pair<Node, SourceRange>> all();
}
