package org.zwobble.shed.parser.tokeniser;

import lombok.Data;

@Data
public class Token {
    private static final Token END = new Token(TokenType.END, null);
    
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
    
    public static Token number(String value) {
        return new Token(TokenType.NUMBER, value);
    }
    
    public static Token string(String value) {
        return new Token(TokenType.STRING, value);
    }
    public static Token unterminatedString(String value) {
        return new Token(TokenType.UNTERMINATED_STRING, value);
    }
    public static Token stringWithInvalidEscapeCodes(String value) {
        return new Token(TokenType.STRING_WITH_INVALID_ESCAPE_CODES, value);
    }
    
    public static Token end() {
        return END;
    }
    
    public static Token error(String value) {
        return new Token(TokenType.ERROR, value);
    }
    
    private final TokenType type;
    private final String value;
    
    public String describe() {
        if (type == TokenType.END) {
            return "end of source";
        }
        return String.format("%s \"%s\"", type.name().toLowerCase(), value);
    }
}
