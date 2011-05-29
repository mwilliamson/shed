package org.zwobble.shed.parser.tokeniser;

public enum Keyword {
    PACKAGE,
    IMPORT,
    VAL;
    
    public String keywordName() {
        return name().toLowerCase();
    }
}
