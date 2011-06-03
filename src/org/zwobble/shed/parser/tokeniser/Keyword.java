package org.zwobble.shed.parser.tokeniser;

public enum Keyword {
    PACKAGE,
    IMPORT,
    VAL,
    VAR,
    PUBLIC;
    
    public String keywordName() {
        return name().toLowerCase();
    }
}
