package org.zwobble.shed.compiler.tokeniser;

public enum TokenType {
    KEYWORD,
    WHITESPACE,
    IDENTIFIER,
    SYMBOL,
    NUMBER,
    STRING,
    END,
    ERROR,
    UNTERMINATED_STRING,
    STRING_WITH_INVALID_ESCAPE_CODES,
    SINGLE_LINE_COMMENT
}
