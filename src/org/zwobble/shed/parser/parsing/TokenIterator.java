package org.zwobble.shed.parser.parsing;

import java.util.List;

import org.zwobble.shed.parser.tokeniser.Token;
import org.zwobble.shed.parser.tokeniser.TokenPosition;
import org.zwobble.shed.parser.tokeniser.TokenType;

public class TokenIterator {
    private final List<TokenPosition> tokens;
    private int nextIndex = 0;

    public TokenIterator(List<TokenPosition> tokens) {
        this.tokens = tokens;
    }
    
    public TokenPosition peek() {
        return tokens.get(nextIndex);
    }
    
    public boolean hasNext() {
        return nextIndex < tokens.size();
    }
    
    public TokenPosition next() {
        nextIndex += 1;
        return tokens.get(nextIndex - 1);
    }
    
    public int currentPosition() {
        return nextIndex;
    }
    
    public void resetPosition(int index) {
        nextIndex = index;
    }

    public void seekToEndOfStatement() {
        while (!peek().getToken().equals(Token.end()) && !next().getToken().equals(new Token(TokenType.SYMBOL, ";"))) {
        }
    }
}
