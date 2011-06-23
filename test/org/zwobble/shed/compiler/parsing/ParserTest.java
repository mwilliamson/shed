package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class ParserTest {
    private final Parser parser = new Parser();
    
    @Test public void
    parsingReturnsMapFromNodesToSourceRanges() {
        ParseResult<SourceNode> parseResult = parser.parse(tokens("package shed.example;\npublic answer;\nval answer = 1;"));
        
        SourceNode sourceNode = parseResult.get();
        assertThat(
            parseResult.locate(sourceNode),
            is(new SourceRange(new SourcePosition(1, 1), new SourcePosition(3, 16)))
        );
        
        assertThat(
            parseResult.locate(sourceNode.getPackageDeclaration()),
            is(new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 22)))
        );
    }
    @Test public void
    mapsFromArgumentNodesToLocations() {
        ParseResult<SourceNode> parseResult = parser.parse(tokens("package blah;\n\npublic Go;\n\nval x = (y: Nuber, z: String) => y;"));
        
        ImmutableVariableNode immutableVariable = (ImmutableVariableNode) parseResult.get().getStatements().get(0);
        ShortLambdaExpressionNode lambda = (ShortLambdaExpressionNode) immutableVariable.getValue();
        
        assertThat(
            parseResult.locate(lambda.getFormalArguments().get(0).getType()),
            is(new SourceRange(new SourcePosition(5, 13), new SourcePosition(5, 18)))
        );
        assertThat(
            parseResult.locate(lambda.getFormalArguments().get(1).getType()),
            is(new SourceRange(new SourcePosition(5, 23), new SourcePosition(5, 29)))
        );
    }
}
