package org.zwobble.shed.parser.parsing;

import org.junit.Test;
import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.StringLiteralNode;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.parser.parsing.ParserTesting.tokens;
import static org.zwobble.shed.parser.parsing.Result.success;

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
