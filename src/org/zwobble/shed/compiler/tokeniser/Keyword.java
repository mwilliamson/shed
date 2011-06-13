package org.zwobble.shed.compiler.tokeniser;

public enum Keyword {
    PACKAGE,
    IMPORT,
    VAL,
    VAR,
    PUBLIC,
    RETURN,
    TRUE,
    FALSE;
    
    public String keywordName() {
        return name().toLowerCase();
    }
}
