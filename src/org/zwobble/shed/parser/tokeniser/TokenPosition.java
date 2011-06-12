package org.zwobble.shed.parser.tokeniser;

import org.zwobble.shed.parser.parsing.SourcePosition;

import lombok.Data;

@Data
public class TokenPosition {
    private final SourcePosition position;
    private final Token token;
}
