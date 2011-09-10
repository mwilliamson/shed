package org.zwobble.shed.compiler.parsing;

import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;

public class TokenStructure {
    private final Map<TokenPosition, TokenPosition> matchingClosingSymbols;
    private final List<TokenPosition> endOfStatements;
    
    public TokenStructure(Map<TokenPosition, TokenPosition> matchingClosingSymbols, List<TokenPosition> endOfStatements) {
        this.matchingClosingSymbols = matchingClosingSymbols;
        this.endOfStatements = endOfStatements;
    }
    
    public TokenPosition findMatchingClosingSymbolFor(TokenPosition openingSymbol) {
        return matchingClosingSymbols.get(openingSymbol);
    }
    
    public boolean hasMatchingClosingSymbol(TokenPosition openingSymbol) {
        return matchingClosingSymbols.containsKey(openingSymbol);
    }

    public Option<TokenPosition> findEndOfStatement(TokenPosition tokenPosition) {
        for (TokenPosition endOfStatement : endOfStatements) {
            if (endOfStatement.getPosition().compareTo(tokenPosition.getPosition()) > 0) {
                return Option.some(endOfStatement);
            }
        }
        return Option.none();
    }

}
