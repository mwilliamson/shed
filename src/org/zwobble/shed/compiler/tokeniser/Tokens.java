package org.zwobble.shed.compiler.tokeniser;

import java.util.List;

public class Tokens implements Iterable<TokenPosition> {
    public static Tokens build(List<TokenPosition> tokens) {
        return new Tokens(tokens);
    }
    
    private final List<TokenPosition> tokens;

    private Tokens(List<TokenPosition> tokens) {
        this.tokens = tokens;
    }

    @Override
    public TokenIterator iterator() {
        return new TokenIterator(tokens);
    }
}
