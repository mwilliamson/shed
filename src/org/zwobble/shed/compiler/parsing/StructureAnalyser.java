package org.zwobble.shed.compiler.parsing;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenIterator;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.Tokens;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class StructureAnalyser {
    private static final Token OPENING_PAREN = Token.symbol("(");
    
    private static final BiMap<Token, Token> openingSymbolToClosingSymbol = ImmutableBiMap.<Token, Token>of(
        Token.symbol("{"), Token.symbol("}"),
        OPENING_PAREN, Token.symbol(")")
    );
    
    public TokenStructure analyse(Tokens tokens) {
        Deque<TokenPosition> openingSymbols = new LinkedList<TokenPosition>();
        Map<TokenPosition, TokenPosition> matchingClosingBraces = new HashMap<TokenPosition, TokenPosition>();
        
        for (TokenPosition tokenPosition : tokens) {
            Token token = tokenPosition.getToken();
            if (isOpeningSymbol(token)) {
                openingSymbols.push(tokenPosition);
            }
            if (isClosingSymbol(token)) {
                while (!openingSymbols.isEmpty() && !openingSymbols.peek().getToken().equals(openingSymbolFor(token))) {
                    openingSymbols.pop();
                }
                if (!openingSymbols.isEmpty()) {
                    matchingClosingBraces.put(openingSymbols.pop(), tokenPosition);                    
                }
            }
            if (token.equals(Token.symbol(";"))) {
                while (!openingSymbols.isEmpty() && openingSymbols.peek().getToken().equals(OPENING_PAREN)) {
                    openingSymbols.pop();
                }
            }
        }
        
        return new TokenStructure(matchingClosingBraces);
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
