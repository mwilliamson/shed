package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class ParserTest {
    private final Parser parser = new Parser();
    
    @Test public void
    parsingReturnsMapFromNodesToSourceRanges() {
        Result<SourceNode> parseResult = parser.parse(tokens("package shed.example;\npublic answer;\nval answer = 1;"));
        
        SourceNode sourceNode = parseResult.get();
        assertThat(
            parseResult.positionOf(sourceNode),
            is(new SourceRange(new SourcePosition(1, 1), new SourcePosition(3, 16)))
        );
        
//        assertThat(
//            parseResult.positionOf(sourceNode.getPackageDeclaration()),
//            is(new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 21)))
//        );
    }
}
