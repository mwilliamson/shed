package org.zwobble.shed.parser.tokeniser;

import lombok.Data;

@Data
public class Token {
    public static Token keyword(Keyword keyword) {
        return new Token(TokenType.KEYWORD, keyword.keywordName());
    }
    
    public static Token symbol(String value) {
        return new Token(TokenType.SYMBOL, value);
    }
    
    public static Token identifier(String value) {
        return new Token(TokenType.IDENTIFIER, value);
    }
    
    public static Token whitespace(String value) {
        return new Token(TokenType.WHITESPACE, value);
    }
    
    private final TokenType type;
    private final String value;
    
    @Override
    public String toString() {
        return String.format("%s \"%s\"", type.name().toLowerCase(), value);
    }
}
