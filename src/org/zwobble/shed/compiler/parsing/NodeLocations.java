package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

public interface NodeLocations {
    SourceRange locate(SyntaxNode node);
}
