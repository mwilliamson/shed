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
    IF,
    ELSE,
    WHILE;
    
    public String keywordName() {
        return name().toLowerCase();
    }
}
