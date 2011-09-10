package org.zwobble.shed.compiler.parsing;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenIterator;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.Tokens;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class StructureAnalyser {
    private static final Token OPENING_BRACE = Token.symbol("{");
    public static final Token CLOSING_BRACE = Token.symbol("}");
    private static final Token OPENING_PAREN = Token.symbol("(");
    
    private static final BiMap<Token, Token> openingSymbolToClosingSymbol = ImmutableBiMap.<Token, Token>of(
        OPENING_BRACE, CLOSING_BRACE,
        OPENING_PAREN, Token.symbol(")")
    );
    
    public TokenStructure analyse(Tokens tokens) {
        Deque<TokenPosition> openingSymbols = new LinkedList<TokenPosition>();
        Builder<TokenPosition, TokenPosition> matchingClosingBraces = ImmutableMap.builder();
        Endings endings = new Endings();
        Builder<TokenPosition, Integer> scopeDepthBeforeSymbol = ImmutableMap.builder();
        
        for (TokenIterator tokenIterator = tokens.iterator(); tokenIterator.hasNext(); ) {
            TokenPosition tokenPosition = tokenIterator.next();
            Token token = tokenPosition.getToken();
            
            scopeDepthBeforeSymbol.put(tokenPosition, openingSymbols.size());
            
            if (isOpeningSymbol(token)) {
                openingSymbols.push(tokenPosition);
            }
            if (isClosingSymbol(token)) {
                Set<Token> poppedTokens = new HashSet<Token>();
                while (!openingSymbols.isEmpty() && !openingSymbols.peek().getToken().equals(openingSymbolFor(token))) {
                    poppedTokens.add(openingSymbols.pop().getToken());
                }
                if (!openingSymbols.isEmpty()) {
                    matchingClosingBraces.put(openingSymbols.pop(), tokenPosition);                    
                }
                if (token.equals(CLOSING_BRACE) || poppedTokens.contains(OPENING_BRACE)) {
                    endings.add(tokenPosition, openingSymbols.size());
                }
            }
            if (token.equals(Token.symbol(";"))) {
                while (!openingSymbols.isEmpty() && openingSymbols.peek().getToken().equals(OPENING_PAREN)) {
                    openingSymbols.pop();
                }
                endings.add(tokenIterator.peek(), openingSymbols.size());
            }
            if (!tokenIterator.hasNext()) {
                endings.add(tokenPosition, 0);
            }
        }

        
        return new TokenStructure(matchingClosingBraces.build(), endings, scopeDepthBeforeSymbol.build());
    }
    
    private Token openingSymbolFor(Token token) {
        return openingSymbolToClosingSymbol.inverse().get(token); 
    }

    private boolean isOpeningSymbol(Token token) {
        return openingSymbolToClosingSymbol.containsKey(token);
    }

    private boolean isClosingSymbol(Token token) {
        return openingSymbolToClosingSymbol.containsValue(token);
    }
}
