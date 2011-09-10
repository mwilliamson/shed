package org.zwobble.shed.compiler.parsing;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.Tokens;

public class StructureAnalyser {
    public TokenStructure analyse(Tokens tokens) {
        Deque<TokenPosition> openingBraces = new LinkedList<TokenPosition>();
        Map<TokenPosition, TokenPosition> matchingClosingBraces = new HashMap<TokenPosition, TokenPosition>();
        
        for (TokenPosition tokenPosition : tokens) {
            Token token = tokenPosition.getToken();
            if (token.equals(Token.symbol("{")) || token.equals(Token.symbol("("))) {
                openingBraces.push(tokenPosition);
            }
            if (token.equals(Token.symbol("}")) || token.equals(Token.symbol(")"))) {
                matchingClosingBraces.put(openingBraces.pop(), tokenPosition);
            }
        }
        
        return new TokenStructure(matchingClosingBraces);
    }
}
