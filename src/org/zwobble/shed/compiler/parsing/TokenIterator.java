package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.tokeniser.TokenPosition;

public class TokenIterator {
    private final List<TokenPosition> tokens;
    private int nextIndex;

    public TokenIterator(List<TokenPosition> tokens) {
        this.tokens = tokens;
        this.nextIndex = 0;
    }

    private TokenIterator(List<TokenPosition> tokens, int nextIndex) {
        this.tokens = tokens;
        this.nextIndex = nextIndex;
    }
    
    public TokenPosition peek() {
        return tokens.get(nextIndex);
    }
    
    public boolean hasNext() {
        return nextIndex < tokens.size();
    }
    
    public TokenPosition next() {
        TokenPosition nextTokenPosition = tokens.get(nextIndex);
        nextIndex += 1;
        return nextTokenPosition;
    }

    public SourcePosition currentPosition() {
        if (!hasNext()) {
            return tokens.get(tokens.size() - 1).getPosition();
        }
        return peek().getPosition();
    }
    
    public TokenIterator currentState() {
        return new TokenIterator(tokens, nextIndex);
    }
    
    public void reset(TokenIterator position) {
        nextIndex = position.nextIndex;
    }
}
