package org.zwobble.shed.compiler.tokeniser;

import org.zwobble.shed.compiler.parsing.SourcePosition;

import lombok.Data;

@Data
public class TokenPosition {
    private final SourcePosition position;
    private final Token token;
}
