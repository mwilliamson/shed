package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.SourceNode;

public class Parser {
    public Result<SourceNode> parse(TokenIterator tokens) {
        return TopLevelNodes.source().parse(tokens);
    }
}
