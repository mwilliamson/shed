package org.zwobble.shed.parser.tokeniser;

public enum TokenType {
    KEYWORD,
    WHITESPACE,
    IDENTIFIER,
    SYMBOL,
    NUMBER,
    STRING,
    END,
    ERROR,
    UNTERMINATED_STRING
}
