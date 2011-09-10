package org.zwobble.shed.compiler.parsing;

import java.util.Map;

import org.zwobble.shed.compiler.tokeniser.TokenPosition;

public class TokenStructure {
    private final Map<TokenPosition, TokenPosition> matchingClosingBraces;
    
    public TokenStructure(Map<TokenPosition, TokenPosition> matchingClosingBraces) {
        this.matchingClosingBraces = matchingClosingBraces;
    }
    
    public TokenPosition findMatchingClosingSymbolFor(TokenPosition openingBrace) {
        return matchingClosingBraces.get(openingBrace);
    }

}
