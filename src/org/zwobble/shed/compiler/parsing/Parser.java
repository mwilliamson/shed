package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNodeIdentifier;

import com.google.common.collect.ImmutableMap;

public class Parser {
    public ParseResult<SourceNode> parse(TokenIterator tokens) {
        SourcePosition start = tokens.currentPosition();
        Result<SourceNode> result = TopLevelNodes.source().parse(tokens);
        SourcePosition end = tokens.currentPosition();
        return new ParseResult<SourceNode>(result.get(), ImmutableMap.of(new SyntaxNodeIdentifier(result.get()), new SourceRange(start, end)));
    }
}
