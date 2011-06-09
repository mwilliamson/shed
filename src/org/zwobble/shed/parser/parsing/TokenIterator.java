package org.zwobble.shed.parser.parsing;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.parser.tokeniser.Token;
import org.zwobble.shed.parser.tokeniser.TokenPosition;

import static org.zwobble.shed.parser.tokeniser.Token.symbol;

public class TokenIterator {
    private final List<TokenPosition> tokens;
    private final List<ScopeType> scopes;
    private int nextIndex;

    public TokenIterator(List<TokenPosition> tokens) {
        this.tokens = tokens;
        this.scopes = new ArrayList<ScopeType>();
        this.nextIndex = 0;
    }

    private TokenIterator(List<TokenPosition> tokens, List<ScopeType> scopes, int nextIndex) {
        this.tokens = tokens;
        this.scopes = scopes;
        this.nextIndex = nextIndex;
    }
    
    public TokenPosition peek() {
        return tokens.get(nextIndex);
    }
    
    public boolean hasNext() {
        return nextIndex < tokens.size();
    }
    
    public TokenPosition next() {
        // TODO: should put types of scope (parens, brackets, square brackets) in a single location (also appears in rules)
        TokenPosition nextTokenPosition = tokens.get(nextIndex);
        Token nextToken = nextTokenPosition.getToken();
        if (nextToken.equals(symbol("{"))) {
            scopes.add(ScopeType.BRACES);
        }
        if (nextToken.equals(symbol("}"))) {
            // Assume that other rules will report an error if nested badly
            while (currentScope() != ScopeType.BRACES) {
                popScope();                
            }
            popScope();
        }
        if (nextToken.equals(symbol("("))) {
            scopes.add(ScopeType.PARENS);
        }
        if (nextToken.equals(symbol(")"))) {
            while (currentScope() != ScopeType.PARENS) {
                popScope();                
            }
            popScope();
        }
        if (nextToken.equals(symbol(";"))) {
            while (currentScope() != ScopeType.BRACES) {
                popScope();
            }
        }
        nextIndex += 1;
        return nextTokenPosition;
    }

    public TokenIterator currentPosition() {
        return new TokenIterator(tokens, new ArrayList<ScopeType>(scopes), nextIndex);
    }
    
    public void resetPosition(TokenIterator position) {
        nextIndex = position.nextIndex;
        scopes.clear();
        scopes.addAll(position.scopes);
    }

    public void seekToEndOfStatement() {
        int initialScopeDepth = scopes.size();
        while (!peek().getToken().equals(Token.end()) &&
            !(initialScopeDepth == scopes.size() && peek().getToken().equals(symbol("}"))) &&
            !next().getToken().equals(symbol(";"))) {
        }
    }

    public void seekToEndOfBlock() {
        int initialScopeDepth = scopes.size();
        while (!(initialScopeDepth == scopes.size() && peek().getToken().equals(symbol("}")))) {
            if (peek().getToken().equals(Token.end())) {
                return;
            }
            next();
        }
        next();
    }
    
    private ScopeType currentScope() {
        if (scopes.isEmpty()) {
            return ScopeType.BRACES;
        }
        return scopes.get(scopes.size() - 1);
    }
    
    private void popScope() {
        if (!scopes.isEmpty()) {
            scopes.remove(scopes.size() - 1);
        }
    }
}
