package org.zwobble.shed.parser.tokeniser;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.charactersOf;
import static java.util.Collections.emptyList;

public class Tokeniser {
    private static final String operatorCharacters = ".;";
    
    public List<Token> tokenise(String inputString) {
        if (inputString.isEmpty()) {
            return emptyList();
        }
        List<Token> tokens = new ArrayList<Token>();
        TokenType currentTokenType = null;
        TokenType previousTokenType = null;
        StringBuilder currentTokenValue = new StringBuilder();
        
        for (char c : charactersOf(inputString)) {
            previousTokenType = currentTokenType;
            if (Character.isWhitespace(c)) {
                currentTokenType = TokenType.WHITESPACE;
            } else if (isSymbolCharacter(c)) {
                currentTokenType = TokenType.SYMBOL;
            } else {
                currentTokenType = TokenType.IDENTIFIER;
            }
            if (previousTokenType != currentTokenType && previousTokenType != null) {
                tokens.add(toToken(currentTokenValue.toString(), previousTokenType));
                currentTokenValue = new StringBuilder();
            }
            currentTokenValue.append(c);
        }
        tokens.add(toToken(currentTokenValue.toString(), currentTokenType));
        return tokens;
    }
    
    private boolean isSymbolCharacter(char c) {
        return operatorCharacters.contains(Character.toString(c));
    }

    private Token toToken(String value, TokenType currentTokenType) {
        if (isKeyword(value)) {
            return new Token(TokenType.KEYWORD, value);
        }
        return new Token(currentTokenType, value);
    }

    private boolean isKeyword(String value) {
        for (Keyword keyword : Keyword.values()) {
            if (value.equals(keyword.keywordName())) {
                return true;
            }
        }
        return false;
    }
}
