package org.zwobble.shed.compiler.parsing;

import java.util.Map;

import org.zwobble.shed.compiler.tokeniser.TokenPosition;

public class TokenStructure {
    private final Map<TokenPosition, TokenPosition> matchingClosingSymbols;
    
    public TokenStructure(Map<TokenPosition, TokenPosition> matchingClosingSymbols) {
        this.matchingClosingSymbols = matchingClosingSymbols;
    }
    
    public TokenPosition findMatchingClosingSymbolFor(TokenPosition openingSymbol) {
        return matchingClosingSymbols.get(openingSymbol);
    }
    
    public boolean hasMatchingClosingSymbol(TokenPosition openingSymbol) {
        return matchingClosingSymbols.containsKey(openingSymbol);
    }

}
