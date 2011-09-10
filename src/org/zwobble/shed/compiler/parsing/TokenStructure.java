package org.zwobble.shed.compiler.parsing;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;

@AllArgsConstructor
public class TokenStructure {
    private final Map<TokenPosition, TokenPosition> matchingClosingSymbols;
    private final List<EndOfStatement> endOfStatements;
    private final Map<TokenPosition, Integer> scopeDepthBeforeSymbol;
    
    public TokenPosition findMatchingClosingSymbolFor(TokenPosition openingSymbol) {
        return matchingClosingSymbols.get(openingSymbol);
    }
    
    public boolean hasMatchingClosingSymbol(TokenPosition openingSymbol) {
        return matchingClosingSymbols.containsKey(openingSymbol);
    }

    public Option<TokenPosition> findEndOfStatement(TokenPosition tokenPosition) {
        for (EndOfStatement endOfStatement : endOfStatements) {
            if (
                endOfStatement.getPosition().compareTo(tokenPosition.getPosition()) > 0 && 
                endOfStatement.getScopeDepth() <= scopeDepthBeforeSymbol.get(tokenPosition)
            ) {
                return Option.some(endOfStatement.getTokenPosition());
            }
        }
        return Option.none();
    }

}
