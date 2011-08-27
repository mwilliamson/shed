package org.zwobble.shed.compiler.parsing;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.parsing.ParserTesting.isSuccessWithNode;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class StatementsTest {
    @Test public void
    canDeclareImmutableVariables() {
        assertThat(
            Statements.immutableVariable().parse(tokens("val magic = 42;")),
            isSuccessWithNode(new ImmutableVariableNode("magic", none(TypeReferenceNode.class), new NumberLiteralNode("42")))
        );
    }
    
    @Test public void
    canDeclareImmutableVariablesWithExplicitType() {
        assertThat(
            Statements.immutableVariable().parse(tokens("val magic : Integer = 42;")),
            isSuccessWithNode(new ImmutableVariableNode("magic", some(new TypeIdentifierNode("Integer")), new NumberLiteralNode("42")))
        );
    }
    
    @Test public void
    canDeclareMutableVariables() {
        assertThat(
            Statements.mutableVariable().parse(tokens("var magic = 42;")),
            isSuccessWithNode(new MutableVariableNode("magic", none(TypeReferenceNode.class), new NumberLiteralNode("42")))
        );
    }
    
    @Test public void
    canDeclareMutableVariablesWithExplicitType() {
        assertThat(
            Statements.mutableVariable().parse(tokens("var magic : Integer = 42;")),
            isSuccessWithNode(new MutableVariableNode("magic", some(new TypeIdentifierNode("Integer")), new NumberLiteralNode("42")))
        );
    }
    
    @Test public void
    canUseExpressionsFollowedByStatementTerminatorAsStatements() {
        assertThat(
            Statements.statement().parse(tokens("alert(\"Full Circle\");")),
            isSuccessWithNode(Nodes.expressionStatement(Nodes.call(Nodes.id("alert"), Nodes.string("Full Circle"))))
        );
    }
    
    @Test public void
    canDeclareAnEmptyObject() {
        assertThat(
            Statements.statement().parse(tokens("object browser { }")),
            isSuccessWithNode(Nodes.object("browser", Collections.<StatementNode>emptyList()))
        );
    }
    
    @Test public void
    canDeclareObjectWithStatements() {
        assertThat(
            Statements.statement().parse(tokens("object browser { val x = 1; }")),
            isSuccessWithNode(Nodes.object("browser", asList(Statements.statement().parse(tokens("val x = 1;")).get())))
        );
    }
    
    @Test public void
    canDeclarePublicObjects() {
        assertThat(
            Statements.statement().parse(tokens("public object browser { }")),
            isSuccessWithNode(Nodes.publik(Nodes.object("browser", Collections.<StatementNode>emptyList())))
        );
    }
    
    @Test public void
    canDeclarePublicVariables() {
        assertThat(
            Statements.statement().parse(tokens("public val magic = 42;")),
            isSuccessWithNode(Nodes.publik(Nodes.immutableVar("magic", Nodes.number("42"))))
        );
    }
}
