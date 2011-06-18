package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;

import static org.junit.Assert.assertThat;
import static org.zwobble.shed.compiler.parsing.ParserTesting.isSuccessWithNode;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class LiteralsTest {
    @Test public void
    canParseNumberLiterals() {
        assertThat(
            Literals.numberLiteral().parse(tokens("42")),
            isSuccessWithNode(new NumberLiteralNode("42"))
        );
    }
    @Test public void
    canParseStringLiterals() {
        assertThat(
            Literals.stringLiteral().parse(tokens("\"Stop giving me verses\"")),
            isSuccessWithNode(new StringLiteralNode("Stop giving me verses"))
        );
    }
}
