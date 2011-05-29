package org.zwobble.shed.parser.parsing;

import org.junit.Test;
import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.tokeniser.Tokeniser;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.parser.parsing.Result.success;

public class LiteralsTest {
    @Test public void
    canParseNumberLiterals() {
        assertThat(Literals.numberLiteral().parse(tokens("42")),
                   is(success(new NumberLiteralNode("42"))));
    }
    
    private TokenIterator tokens(String input) {
        return new TokenIterator(new Tokeniser().tokenise(input));
    }
}
