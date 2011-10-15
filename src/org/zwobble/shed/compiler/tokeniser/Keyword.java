package org.zwobble.shed.compiler.tokeniser;

public enum Keyword {
    PACKAGE,
    IMPORT,
    VAL,
    VAR,
    PUBLIC,
    RETURN,
    TRUE,
    FALSE,
    OBJECT,
    CLASS,
    IF,
    ELSE,
    WHILE,
    FUN;
    
    public String keywordName() {
        return name().toLowerCase();
    }
}
