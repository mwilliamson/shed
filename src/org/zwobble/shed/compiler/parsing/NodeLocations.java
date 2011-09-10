package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.Node;

public interface NodeLocations {
    SourceRange locate(Node node);
}
