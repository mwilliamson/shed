package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.Literals;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;

import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

import static org.zwobble.shed.compiler.parsing.Result.success;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LiteralsTest {
    @Test public void
    canParseNumberLiterals() {
        assertThat(Literals.numberLiteral().parse(tokens("42")),
                   is(success(new NumberLiteralNode("42"))));
    }
    @Test public void
    canParseStringLiterals() {
        assertThat(Literals.stringLiteral().parse(tokens("\"Stop giving me verses\"")),
                   is(success(new StringLiteralNode("Stop giving me verses"))));
    }
}
