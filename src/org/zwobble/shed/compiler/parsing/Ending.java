package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.tokeniser.TokenPosition;

import lombok.Data;

@Data
public class Ending {
    private final TokenPosition tokenPosition;
    private final int scopeDepth;
    
    public SourcePosition getPosition() {
        return tokenPosition.getPosition();
    }
}
