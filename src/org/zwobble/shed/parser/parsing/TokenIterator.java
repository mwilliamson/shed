package org.zwobble.shed.parser.parsing;

import java.util.List;
import java.util.Stack;

import org.zwobble.shed.parser.tokeniser.TokenPosition;

public class TokenIterator {
    private final List<TokenPosition> tokens;
    private final Stack<Integer> positions = new Stack<Integer>();
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
    
    public void savePosition() {
        positions.push(nextIndex);
    }
    
    public void revertPosition() {
        nextIndex = positions.pop();
    }
}
