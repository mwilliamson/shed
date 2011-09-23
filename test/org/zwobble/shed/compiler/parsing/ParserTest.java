package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class ParserTest {
    private final Parser parser = new Parser();
    
    @Test public void
    parsingReturnsMapFromNodesToSourceRanges() {
        ParseResult<SourceNode> parseResult = parser.parse(tokens("package shed.example;\nval answer = 1;"));
        
        SourceNode sourceNode = parseResult.get();
        assertThat(
            parseResult.locate(sourceNode),
            is(new SourceRange(new SourcePosition(1, 1), new SourcePosition(2, 16)))
        );
        
        assertThat(
            parseResult.locate(sourceNode.getPackageDeclaration()),
            is(new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 22)))
        );
    }
    @Test public void
    mapsFromArgumentNodesToLocations() {
        ParseResult<SourceNode> parseResult = parser.parse(tokens("package blah;\n\nval x = (y: Nuber, z: String) => y;"));
        
        VariableDeclarationNode immutableVariable = (VariableDeclarationNode) parseResult.get().getStatements().get(0);
        ShortLambdaExpressionNode lambda = (ShortLambdaExpressionNode) immutableVariable.getValue();
        
        assertThat(
            parseResult.locate(lambda.getFormalArguments().get(0).getType()),
            is(new SourceRange(new SourcePosition(3, 13), new SourcePosition(3, 18)))
        );
        assertThat(
            parseResult.locate(lambda.getFormalArguments().get(1).getType()),
            is(new SourceRange(new SourcePosition(3, 23), new SourcePosition(3, 29)))
        );
    }
}
