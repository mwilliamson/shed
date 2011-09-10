package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.SourceNode;

public class Parser {
    public ParseResult<SourceNode> parse(TokenNavigator tokens) {
        return TopLevelNodes.source().parse(tokens);
    }
}
