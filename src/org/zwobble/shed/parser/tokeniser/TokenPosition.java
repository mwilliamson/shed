package org.zwobble.shed.parser.tokeniser;

import lombok.Data;

@Data
public class TokenPosition {
    private final int lineNumber;
    private final int characterNumber;
    private final Token token;
}
