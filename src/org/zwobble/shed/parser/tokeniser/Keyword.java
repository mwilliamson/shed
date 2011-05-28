package org.zwobble.shed.parser.tokeniser;

public enum Keyword {
    PACKAGE {
        @Override
        public String keywordName() {
            return "package";
        }
    };
    public abstract String keywordName();
    
}
