package org.zwobble.shed.compiler.tokeniser;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.SourcePosition;
import org.zwobble.shed.compiler.parsing.SourceRange;

@Data
public class TokenPosition {
    private final SourceRange sourceRange;
    private final Token token;
    
    public SourcePosition getStartPosition() {
        return sourceRange.getStart();
    }
    
    public SourcePosition getEndPosition() {
        return sourceRange.getEnd();
    }
}
