package org.zwobble.shed.compiler.tokeniser;

import lombok.Data;

@Data
public class Token {
    private static final Token END = new Token(TokenType.END, null, null);
    
    public static Token keyword(Keyword keyword) {
        return new Token(TokenType.KEYWORD, keyword.keywordName(), keyword.keywordName());
    }
    
    public static Token symbol(String value) {
        return new Token(TokenType.SYMBOL, value, value);
    }
    
    public static Token identifier(String value) {
        return new Token(TokenType.IDENTIFIER, value, value);
    }
    
    public static Token whitespace(String value) {
        return new Token(TokenType.WHITESPACE, value, value);
    }
    
    public static Token number(String value) {
        return new Token(TokenType.NUMBER, value, value);
    }
    
    public static Token string(String value, String sourceString) {
        return new Token(TokenType.STRING, value, sourceString);
    }
    public static Token unterminatedString(String value, String sourceString) {
        return new Token(TokenType.UNTERMINATED_STRING, value, sourceString);
    }
    public static Token stringWithInvalidEscapeCodes(String value, String sourceString) {
        return new Token(TokenType.STRING_WITH_INVALID_ESCAPE_CODES, value, sourceString);
    }
    public static Token singleLineComment(String value) {
        return new Token(TokenType.SINGLE_LINE_COMMENT, value, "//" + value);
    }
    
    public static Token end() {
        return END;
    }
    
    public static Token error(String value) {
        return new Token(TokenType.ERROR, value, value);
    }
    
    public static Token token(TokenType type, String value) {
        return new Token(type, value, value);
    }
    
    private final TokenType type;
    private final String value;
    private final String sourceString;
    
    public String describe() {
        if (type == TokenType.END) {
            return "end of source";
        }
        return String.format("%s \"%s\"", type.name().toLowerCase(), value);
    }
}
