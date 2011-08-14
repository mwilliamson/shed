package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;

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
}
