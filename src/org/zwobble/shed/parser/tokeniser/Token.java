package org.zwobble.shed.parser.tokeniser;

import lombok.Data;

@Data
public class Token {
    public static Token keyword(String value) {
        return new Token(TokenType.KEYWORD, value);
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
}
