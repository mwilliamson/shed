package org.zwobble.shed.compiler.tokeniser;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.compiler.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.compiler.tokeniser.Token.error;
import static org.zwobble.shed.compiler.tokeniser.Token.identifier;
import static org.zwobble.shed.compiler.tokeniser.Token.keyword;
import static org.zwobble.shed.compiler.tokeniser.Token.number;
import static org.zwobble.shed.compiler.tokeniser.Token.string;
import static org.zwobble.shed.compiler.tokeniser.Token.symbol;
import static org.zwobble.shed.compiler.tokeniser.Token.token;
import static org.zwobble.shed.compiler.tokeniser.Token.unterminatedString;
import static org.zwobble.shed.compiler.tokeniser.Token.whitespace;

public class TokeniserTest {
    private final Tokeniser tokeniser = new Tokeniser();
    
    @Test public void
    emptyStringIsSingletonOfEndToken() {
        assertThat(tokens(""), is(asList(Token.end())));
    }
    
    @Test public void
    tokeniseKeyword() {
        assertThat(tokens("package"), is(asList(token(TokenType.KEYWORD, "package"), Token.end())));
    }
    
    @Test public void
    tokeniseIdentifier() {
        assertThat(tokens("bob"), is(asList(token(TokenType.IDENTIFIER, "bob"), Token.end())));
    }
    
    @Test public void
    tokeniseSymbol() {
        assertThat(tokens("."), is(asList(token(TokenType.SYMBOL, "."), Token.end())));
    }
    
    @Test public void
    consecutiveSymbolsAreTreatedAsSeparateSymbols() {
        assertThat(tokens("()"), is(asList(token(TokenType.SYMBOL, "("), token(TokenType.SYMBOL, ")"), Token.end())));
    }
    
    @Test public void
    canParseMultiCharacterSymbols() {
        assertThat(tokens("=>"), is(asList(token(TokenType.SYMBOL, "=>"), Token.end())));
    }
    
    @Test public void
    tokenisePackageDeclaration() {
        assertThat(tokens("package shed.util.collections;"), is(asList(
            keyword(PACKAGE),
            whitespace(" "),
            identifier("shed"),
            symbol("."),
            identifier("util"),
            symbol("."),
            identifier("collections"),
            symbol(";"),
            Token.end()
        )));
    }
    
    @Test public void
    identifiersCanContainNumbers() {
        assertThat(tokens("a42"), is(asList(identifier("a42"), Token.end())));
    }
    
    @Test public void
    digitsAreTokenisedIntoANumber() {
        assertThat(tokens("42"), is(asList(number("42"), Token.end())));
    }
    
    @Test public void
    numbersCannotContainsLetters() {
        assertThat(tokens("42ab"), is(asList(number("42"), error("ab"), Token.end())));
    }
    
    @Test public void
    identifiersCannotStartWithDoubleUnderscores() {
        assertThat(tokens("__blah"), is(asList(error("__blah"), Token.end())));
    }
    
    @Test public void
    numbersAfterWhitespace() {
        assertThat(tokens(" 42"), is(asList(whitespace(" "), number("42"), Token.end())));
    }
    
    @Test public void
    simpleStrings() {
        assertThat(tokens("\"Hello!\""), is(asList(string("Hello!", "\"Hello!\""), Token.end())));
    }
    
    @Test public void
    unterminatedStringsDueToEndOfInput() {
        assertThat(tokens("\"Hello!"), is(asList(unterminatedString("Hello!", "\"Hello!"), Token.end())));
    }
    
    @Test public void
    unterminatedStringsDueNewLine() {
        assertThat(tokens("\"Hello!\n"), is(asList(unterminatedString("Hello!", "\"Hello!"), whitespace("\n"), Token.end())));
    }
    
    @Test public void
    escapingSpecialCharactersInStrings() {
        assertThat(tokens("\"\\\"\\b\\t\\n\\f\\r\\'\\\\\""), is(asList(string("\"\b\t\n\f\r'\\", "\"\\\"\\b\\t\\n\\f\\r\\'\\\\\""), Token.end())));
    }
    
    @Test public void
    invalidStringEscapeCodes() {
        assertThat(tokens("\" 53\\yz\""), is(asList(Token.stringWithInvalidEscapeCodes(" 53z", "\" 53\\yz\""), Token.end())));
    }
    
    private List<Token> tokens(String input) {
        return newArrayList(transform(tokeniser.tokenise(input), toToken()));
    }

    private Function<TokenPosition, Token> toToken() {
        return new Function<TokenPosition, Token>() {
            @Override
            public Token apply(TokenPosition input) {
                return input.getToken();
            }
        };
    }
}
