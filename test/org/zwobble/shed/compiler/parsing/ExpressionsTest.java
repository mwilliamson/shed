package org.zwobble.shed.compiler.parsing;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

import static org.zwobble.shed.compiler.parsing.ParserTesting.isSuccessWithNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class ExpressionsTest {
    @Test public void
    numberLiteralIsExpression() {
        assertThat(
            Expressions.expression().parse(tokens("42")),
            isSuccessWithNode(new NumberLiteralNode("42"))
        );
    }
    
    @Test public void
    stringLiteralIsExpression() {
        assertThat(
            Expressions.expression().parse(tokens("\"Nom nom nom\"")),
            isSuccessWithNode(new StringLiteralNode("Nom nom nom"))
        );
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
            isSuccessWithNode(new ShortLambdaExpressionNode(
                Collections.<FormalArgumentNode>emptyList(),
                none(TypeReferenceNode.class),
                new NumberLiteralNode("2")
            ))
        );
    }
    
    @Test public void
    canParseShortLambdaExpressionWithOneArgument() {
        assertThat(
            Expressions.expression().parse(tokens("(num : Integer) => 2")),
            isSuccessWithNode(new ShortLambdaExpressionNode(
                asList(new FormalArgumentNode("num", new TypeIdentifierNode("Integer"))),
                none(TypeReferenceNode.class),
                new NumberLiteralNode("2")
            ))
        );
    }
    
    @Test public void
    canParseShortLambdaExpressionWithMultipleArguments() {
        assertThat(
            Expressions.expression().parse(tokens("(num : Integer, name: String) => 2")),
            isSuccessWithNode(new ShortLambdaExpressionNode(
                asList(
                    new FormalArgumentNode("num", new TypeIdentifierNode("Integer")),
                    new FormalArgumentNode("name", new TypeIdentifierNode("String"))
                ),
                none(TypeReferenceNode.class),
                new NumberLiteralNode("2")
            ))
        );
    }
    
    @Test public void
    canParseLongLambdaExpressionWithNoArguments() {
        assertThat(
            Expressions.expression().parse(tokens("() : String => { val x = 2; return 3; }")),
            isSuccessWithNode(new LongLambdaExpressionNode(
                Collections.<FormalArgumentNode>emptyList(),
                new TypeIdentifierNode("String"),
                asList(
                    new ImmutableVariableNode("x", Option.<TypeReferenceNode>none(), new NumberLiteralNode("2")),
                    new ReturnNode(new NumberLiteralNode("3"))
                )
            ))
        );
    }
    
    @Test public void
    canEncloseExpressionInParens() {
        assertThat(
            Expressions.expression().parse(tokens("(3)")),
            isSuccessWithNode(new NumberLiteralNode("3"))
        );
    }
    
    @Test public void
    canReferenceVariableByIdentifier() {
        assertThat(
            Expressions.expression().parse(tokens("value")),
            isSuccessWithNode(new VariableIdentifierNode("value"))
        );
    }
    
    @Test public void
    canParseTrueLiteral() {
        assertThat(
            Expressions.expression().parse(tokens("true")),
            isSuccessWithNode(new BooleanLiteralNode(true))
        );
    }
    
    @Test public void
    canParseFalseLiteral() {
        assertThat(
            Expressions.expression().parse(tokens("false")),
            isSuccessWithNode(new BooleanLiteralNode(false))
        );
    }
    
    @Test public void
    canParseSingleFunctionCallWithNoArguments() {
        assertThat(
            Expressions.expression().parse(tokens("now()")),
            isSuccessWithNode(new CallNode(new VariableIdentifierNode("now"), Collections.<ExpressionNode>emptyList()))
        );
    }
}
