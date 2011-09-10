package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.TokenType;

import com.google.common.base.Predicate;
import com.google.common.collect.PeekingIterator;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

public class TokenIterator implements PeekingIterator<TokenPosition> {
    public static TokenIterator semanticallySignificantIterator(List<TokenPosition> tokens) {
        return new TokenIterator(newArrayList(filter(tokens, isSemanticallySignificantToken())), 0);
    }
    
    private static Predicate<TokenPosition> isSemanticallySignificantToken() {
        return new Predicate<TokenPosition>() {
            @Override
            public boolean apply(TokenPosition input) {
                TokenType type = input.getToken().getType();
                return type != TokenType.WHITESPACE;
            }
        };
    }
    
    private final List<TokenPosition> tokens;
    private int nextIndex;

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

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
