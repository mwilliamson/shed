package org.zwobble.shed.parser.tokeniser;

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
