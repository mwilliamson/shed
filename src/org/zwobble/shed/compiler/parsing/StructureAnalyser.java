package org.zwobble.shed.compiler.parsing;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenIterator;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.Tokens;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class StructureAnalyser {
    private static final Token OPENING_PAREN = Token.symbol("(");
    private static final Token CLOSING_BRACE = Token.symbol("}");
    
    private static final BiMap<Token, Token> openingSymbolToClosingSymbol = ImmutableBiMap.<Token, Token>of(
        Token.symbol("{"), CLOSING_BRACE,
        OPENING_PAREN, Token.symbol(")")
    );
    
    public TokenStructure analyse(Tokens tokens) {
        Deque<TokenPosition> openingSymbols = new LinkedList<TokenPosition>();
        Builder<TokenPosition, TokenPosition> matchingClosingBraces = ImmutableMap.builder();
        List<TokenPosition> endOfStatements = new ArrayList<TokenPosition>();
        
        for (TokenIterator tokenIterator = tokens.iterator(); tokenIterator.hasNext(); ) {
            TokenPosition tokenPosition = tokenIterator.next();
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
                if (token.equals(CLOSING_BRACE)) {
                    endOfStatements.add(tokenPosition);
                }
            }
            if (token.equals(Token.symbol(";"))) {
                endOfStatements.add(tokenIterator.peek());
                while (!openingSymbols.isEmpty() && openingSymbols.peek().getToken().equals(OPENING_PAREN)) {
                    openingSymbols.pop();
                }
            }
        }
        
        return new TokenStructure(matchingClosingBraces.build(), endOfStatements);
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
