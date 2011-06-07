package org.zwobble.shed.parser.parsing;

import org.junit.Test;
import org.zwobble.shed.parser.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.parser.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.parser.tokeniser.Tokeniser;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.parser.Option.none;
import static org.zwobble.shed.parser.Option.some;
import static org.zwobble.shed.parser.parsing.Result.success;

public class StatementsTest {
    @Test public void
    canDeclareImmutableVariables() {
        assertThat(Statements.immutableVariable().parse(tokens("val magic = 42;")),
                   is(success(new ImmutableVariableNode("magic", none(TypeReferenceNode.class), new NumberLiteralNode("42")))));
    }
    
    @Test public void
    canDeclareImmutableVariablesWithExplicitType() {
        assertThat(Statements.immutableVariable().parse(tokens("val magic : Integer = 42;")),
                   is(success(new ImmutableVariableNode("magic", some(new TypeIdentifierNode("Integer")), new NumberLiteralNode("42")))));
    }
    
    @Test public void
    canDeclareMutableVariables() {
        assertThat(Statements.mutableVariable().parse(tokens("var magic = 42;")),
                   is(success(new MutableVariableNode("magic", none(TypeReferenceNode.class), new NumberLiteralNode("42")))));
    }
    
    @Test public void
    canDeclareMutableVariablesWithExplicitType() {
        assertThat(Statements.mutableVariable().parse(tokens("var magic : Integer = 42;")),
                   is(success(new MutableVariableNode("magic", some(new TypeIdentifierNode("Integer")), new NumberLiteralNode("42")))));
    }
    
    private TokenIterator tokens(String input) {
        return new TokenIterator(new Tokeniser().tokenise(input));
    }
}
