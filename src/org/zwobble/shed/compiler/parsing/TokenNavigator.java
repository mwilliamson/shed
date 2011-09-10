package org.zwobble.shed.compiler.parsing;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenIterator;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.Tokens;

import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.any;
import static org.zwobble.shed.compiler.tokeniser.Token.symbol;


public class TokenNavigator {
    private final TokenIterator tokens;
    private final List<ScopeType> scopes;

    public TokenNavigator(Tokens tokens) {
        this.tokens = tokens.iterator();
        this.scopes = new ArrayList<ScopeType>();
    }

    private TokenNavigator(TokenIterator tokens, List<ScopeType> scopes) {
        this.tokens = tokens;
        this.scopes = scopes;
    }
    
    public TokenPosition peek() {
        return tokens.peek();
    }
    
    public boolean hasNext() {
        return tokens.hasNext();
    }
    
    public TokenPosition next() {
        // TODO: should put types of scope (parens, brackets, square brackets) in a single location (also appears in rules)
        TokenPosition nextTokenPosition = tokens.peek();
        Token nextToken = nextTokenPosition.getToken();
        if (nextToken.equals(symbol("{"))) {
            scopes.add(ScopeType.BRACES);
        }
        if (nextToken.equals(symbol("}"))) {
            // Assume that other rules will report an error if nested badly
            while (currentScope() != ScopeType.BRACES) {
                popScope();                
            }
            if (!scopes.isEmpty()) {
                popScope();                
            }
        }
        if (nextToken.equals(symbol("("))) {
            scopes.add(ScopeType.PARENS);
        }
        if (nextToken.equals(symbol(")"))) {
            popParensScope();
        }
        if (nextToken.equals(symbol(";"))) {
            while (currentScope() != ScopeType.BRACES) {
                popScope();
            }
        }
        tokens.next();
        return nextTokenPosition;
    }

    private void popParensScope() {
        if (any(scopes, isScopeType(ScopeType.PARENS))) {
            while (currentScope() != ScopeType.PARENS) {
                popScope();
            }
            popScope();   
        }
    }

    private Predicate<ScopeType> isScopeType(final ScopeType scopeType) {
        return new Predicate<ScopeType>() {
            @Override
            public boolean apply(ScopeType input) {
                return input == scopeType;
            }
        };
    }

    public SourcePosition currentPosition() {
        return tokens.currentPosition();
    }
    
    public TokenNavigator currentState() {
        return new TokenNavigator(tokens.currentState(), new ArrayList<ScopeType>(scopes));
    }
    
    public void reset(TokenNavigator position) {
        tokens.reset(position.tokens);
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
        scopes.remove(scopes.size() - 1);
    }
}
