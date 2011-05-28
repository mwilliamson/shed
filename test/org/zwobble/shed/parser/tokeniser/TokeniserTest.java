package org.zwobble.shed.parser.tokeniser;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.zwobble.shed.parser.tokeniser.Token.identifier;

import static org.zwobble.shed.parser.tokeniser.Token.symbol;

import static org.zwobble.shed.parser.tokeniser.Token.whitespace;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.parser.tokeniser.Token.keyword;

public class TokeniserTest {
    private final Tokeniser tokeniser = new Tokeniser();
    
    @Test public void
    emptyStringIsEmptyListOfTokens() {
        assertThat(tokeniser.tokenise(""), Matchers.<Token>empty());
    }
    
    @Test public void
    tokeniseWhitespace() {
        assertThat(tokeniser.tokenise("  \n\t \n\t\r"), is(asList(new Token(TokenType.WHITESPACE, "  \n\t \n\t\r"))));
    }
    
    @Test public void
    tokeniseKeyword() {
        assertThat(tokeniser.tokenise("package"), is(asList(new Token(TokenType.KEYWORD, "package"))));
    }
    
    @Test public void
    tokeniseIdentifier() {
        assertThat(tokeniser.tokenise("bob"), is(asList(new Token(TokenType.IDENTIFIER, "bob"))));
    }
    
    @Test public void
    tokeniseSymbol() {
        assertThat(tokeniser.tokenise("."), is(asList(new Token(TokenType.SYMBOL, "."))));
    }
    
    @Test public void
    tokenisePackageDeclaration() {
        assertThat(tokeniser.tokenise("package shed.util.collections;"), is(asList(
            keyword("package"),
            whitespace(" "),
            identifier("shed"),
            symbol("."),
            identifier("util"),
            symbol("."),
            identifier("collections"),
            symbol(";")
        )));
    }
}
