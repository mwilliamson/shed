package org.zwobble.shed.parser.tokeniser;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Function;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.parser.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.parser.tokeniser.Token.identifier;
import static org.zwobble.shed.parser.tokeniser.Token.keyword;
import static org.zwobble.shed.parser.tokeniser.Token.symbol;
import static org.zwobble.shed.parser.tokeniser.Token.whitespace;

public class TokeniserTest {
    private final Tokeniser tokeniser = new Tokeniser();
    
    @Test public void
    emptyStringIsSingletonOfEndToken() {
        assertThat(tokens(""), is(asList(Token.end())));
    }
    
    @Test public void
    tokeniseKeyword() {
        assertThat(tokens("package"), is(asList(new Token(TokenType.KEYWORD, "package"), Token.end())));
    }
    
    @Test public void
    tokeniseIdentifier() {
        assertThat(tokens("bob"), is(asList(new Token(TokenType.IDENTIFIER, "bob"), Token.end())));
    }
    
    @Test public void
    tokeniseSymbol() {
        assertThat(tokens("."), is(asList(new Token(TokenType.SYMBOL, "."), Token.end())));
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
    
    private List<Token> tokens(String input) {
        return transform(tokeniser.tokenise(input), toToken());
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
