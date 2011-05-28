package org.zwobble.shed.parser.tokeniser;

public enum Keyword {
    PACKAGE,
    IMPORT;
    
    public String keywordName() {
        return name().toLowerCase();
    }
}
