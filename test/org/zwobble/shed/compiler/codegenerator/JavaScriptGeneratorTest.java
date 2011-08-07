package org.zwobble.shed.compiler.codegenerator;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.Option.none;

public class JavaScriptGeneratorTest {
    private final JavaScriptGenerator generator = new JavaScriptGenerator(null);
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Test public void
    booleanLiteralsAreConvertedToBoxedBooleans() {
        booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(true);
        booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(false);
    }
    
    private void booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(boolean value) {
        BooleanLiteralNode source = new BooleanLiteralNode(value);
        assertGeneratedJavaScript(source, js.call(js.id("SHED.shed.lang.Boolean"), js.bool(value)));
    }
    
    @Test public void
    numberLiteralsAreConvertedToBoxedNumbers() {
        NumberLiteralNode source = new NumberLiteralNode("4.2");
        assertGeneratedJavaScript(source, js.call(js.id("SHED.shed.lang.Number"), js.number("4.2")));
    }
    
    @Test public void
    stringLiteralsAreConvertedToBoxedStrings() {
        StringLiteralNode source = new StringLiteralNode("Stop giving me verses");
        assertGeneratedJavaScript(source, js.call(js.id("SHED.shed.lang.String"), js.string("Stop giving me verses")));
    }
    
    @Test public void
    immutableVariableNodesAreConvertedToVariableDeclarations() {
        ImmutableVariableNode source = new ImmutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.var("x", generator.generate(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    mutableVariableNodesAreConvertedToVariableDeclarations() {
        MutableVariableNode source = new MutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.var("x", generator.generate(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    shortLambdaExpressionWithoutArgumentsIsConvertedIntoJavaScriptAnonymousFunction() {
        ShortLambdaExpressionNode source = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(TypeReferenceNode.class),
            new BooleanLiteralNode(true)
        );
        assertGeneratedJavaScript(source, js.func(Collections.<String>emptyList(), asList(generator.generate(new BooleanLiteralNode(true)))));
    }
    
    @Test public void
    shortLambdaExpressionWithArgumentsIsConvertedIntoJavaScriptAnonymousFunction() {
        ShortLambdaExpressionNode source = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new TypeIdentifierNode("String")),
                new FormalArgumentNode("age", new TypeIdentifierNode("Number"))
            ),
            none(TypeReferenceNode.class),
            new BooleanLiteralNode(true)
        );
        assertGeneratedJavaScript(source, js.func(asList("name", "age"), asList(generator.generate(new BooleanLiteralNode(true)))));
    }
    
    @Test public void
    returnStatementIsConvertedToJavaScriptReturn() {
        ReturnNode source = new ReturnNode(new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.ret(generator.generate(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    longLambdaExpressionIsConvertedIntoJavaScriptAnonymousFunction() {
        ImmutableVariableNode variableNode = new ImmutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true));
        ReturnNode returnNode = new ReturnNode(new NumberLiteralNode("42"));
        LongLambdaExpressionNode source = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new TypeIdentifierNode("String")),
                new FormalArgumentNode("age", new TypeIdentifierNode("Number"))
            ),
            new TypeIdentifierNode("Number"),
            asList(variableNode, returnNode)
        );
        assertGeneratedJavaScript(
            source,
            js.func(asList("name", "age"), asList(generator.generate(variableNode), generator.generate(returnNode)))
        );
    }
    
    private void assertGeneratedJavaScript(SyntaxNode source, JavaScriptNode expectedJavaScript) {
        assertThat(generator.generate(source), is(expectedJavaScript));
    }
}
