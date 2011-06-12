package org.zwobble.shed.parser.parsing;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.parser.Option;
import org.zwobble.shed.parser.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.parser.parsing.nodes.FunctionNode;
import org.zwobble.shed.parser.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.ReturnNode;
import org.zwobble.shed.parser.parsing.nodes.StatementNode;
import org.zwobble.shed.parser.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.parser.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.parser.parsing.nodes.VariableIdentifierNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.parser.parsing.ParserTesting.errorStrings;
import static org.zwobble.shed.parser.parsing.ParserTesting.tokens;

public class ExpressionsTest {
    @Test public void
    numberLiteralIsExpression() {
        assertThat(Expressions.expression().parse(tokens("42")),
            is((Object)Result.success(new NumberLiteralNode("42"))));
    }
    
    @Test public void
    stringLiteralIsExpression() {
        assertThat(Expressions.expression().parse(tokens("\"Nom nom nom\"")),
            is((Object)Result.success(new StringLiteralNode("Nom nom nom"))));
    }
    
    @Test public void
    errorIfValueIsNotExpression() {
        assertThat(
            errorStrings(Expressions.expression().parse(tokens("{"))),
            is(asList("Expected expression but got symbol \"{\""))
        );
    }
    
    @Test public void
    canParseShortLambdaExpressionWithNoArguments() {
        assertThat(
            Expressions.expression().parse(tokens("() => 2")),
            is((Object)Result.success(new FunctionNode(
                Collections.<FormalArgumentNode>emptyList(),
                asList((StatementNode)new ReturnNode(new NumberLiteralNode("2")))
            )))
        );
    }
    
    @Test public void
    canParseShortLambdaExpressionWithOneArgument() {
        assertThat(
            Expressions.expression().parse(tokens("(num : Integer) => 2")),
            is((Object)Result.success(new FunctionNode(
                Arrays.<FormalArgumentNode>asList(new FormalArgumentNode("num", new TypeIdentifierNode("Integer"))),
                Arrays.<StatementNode>asList(new ReturnNode(new NumberLiteralNode("2")))
            )))
        );
    }
    
    @Test public void
    canParseShortLambdaExpressionWithMultipleArgument() {
        assertThat(
            Expressions.expression().parse(tokens("(num : Integer, name: String) => 2")),
            is((Object)Result.success(new FunctionNode(
                asList(
                    new FormalArgumentNode("num", new TypeIdentifierNode("Integer")),
                    new FormalArgumentNode("name", new TypeIdentifierNode("String"))
                ),
                Arrays.<StatementNode>asList(new ReturnNode(new NumberLiteralNode("2")))
            )))
        );
    }
    
    @Test public void
    canParseLongLambdaExpressionWithNoArguments() {
        assertThat(
            Expressions.expression().parse(tokens("() => { val x = 2; return 3; }")),
            is((Object)Result.success(new FunctionNode(
                Collections.<FormalArgumentNode>emptyList(),
                asList(
                    new ImmutableVariableNode("x", Option.<TypeReferenceNode>none(), new NumberLiteralNode("2")),
                    new ReturnNode(new NumberLiteralNode("3"))
                )
            )))
        );
    }
    
    @Test public void
    canEncloseExpressionInParens() {
        assertThat(
            Expressions.expression().parse(tokens("(3)")),
            is((Object)Result.success(new NumberLiteralNode("3")))
        );
    }
    
    @Test public void
    canReferenceVariableByIdentifier() {
        assertThat(
            Expressions.expression().parse(tokens("value")),
            is((Object)(Result.success(new VariableIdentifierNode("value"))))
        );
    }
    
    @Test public void
    canParseTrueLiteral() {
        assertThat(
            Expressions.expression().parse(tokens("true")),
            is((Object)(Result.success(new BooleanLiteralNode(true))))
        );
    }
    
    @Test public void
    canParseFalseLiteral() {
        assertThat(
            Expressions.expression().parse(tokens("false")),
            is((Object)(Result.success(new BooleanLiteralNode(false))))
        );
    }
}
