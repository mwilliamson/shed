package org.zwobble.shed.compiler.parsing;

import java.util.Map;

import lombok.AllArgsConstructor;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;

@AllArgsConstructor
public class TokenStructure {
    private final Map<TokenPosition, TokenPosition> matchingClosingSymbols;
    private final Endings endings;
    private final Map<TokenPosition, Integer> scopeDepthBeforeSymbol;
    
    public TokenPosition findMatchingClosingSymbolFor(TokenPosition openingSymbol) {
        return matchingClosingSymbols.get(openingSymbol);
    }
    
    public boolean hasMatchingClosingSymbol(TokenPosition openingSymbol) {
        return matchingClosingSymbols.containsKey(openingSymbol);
    }

    public Option<TokenPosition> findFirstTokenAfterStatement(TokenPosition tokenPosition) {
        return findFirstEndingAfterPosition(tokenPosition, endings.endsOfStatements());
    }

    public Option<TokenPosition> findFirstTokenAfterBlock(TokenPosition tokenPosition) {
        return findFirstEndingAfterPosition(tokenPosition, endings.endsOfBlocks());
    }

    private Option<TokenPosition> findFirstEndingAfterPosition(TokenPosition tokenPosition, Iterable<Ending> endings) {
        for (Ending ending : endings) {
            if (
                ending.getPosition().compareTo(tokenPosition.getPosition()) > 0 && 
                ending.getScopeDepth() <= scopeDepthBeforeSymbol.get(tokenPosition)
            ) {
                return Option.some(ending.getTokenPosition());
            }
        }
        return Option.none();
    }
}
