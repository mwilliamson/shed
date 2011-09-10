package org.zwobble.shed.compiler.parsing;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.Tokens;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class StructureAnalyser {
    public TokenStructure analyse(Tokens tokens) {
        Map<TokenPosition, TokenPosition> matchingClosingBraces = new HashMap<TokenPosition, TokenPosition>();
        matchingClosingBraces.put(Iterables.find(tokens, openingBrace()), Iterables.find(tokens, closingBrace()));
        
        return new TokenStructure(matchingClosingBraces);
    }

    private Predicate<TokenPosition> openingBrace() {
        return new Predicate<TokenPosition>() {
            @Override
            public boolean apply(TokenPosition input) {
                return input.getToken().equals(Token.symbol("{"));
            }
        };
    }

    private Predicate<TokenPosition> closingBrace() {
        return new Predicate<TokenPosition>() {
            @Override
            public boolean apply(TokenPosition input) {
                return input.getToken().equals(Token.symbol("}"));
            }
        };
    }
}
