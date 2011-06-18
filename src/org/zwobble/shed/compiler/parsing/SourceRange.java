package org.zwobble.shed.compiler.parsing;

import lombok.Data;

@Data
public class SourceRange {
    private final SourcePosition start;
    private final SourcePosition end;
    
    public boolean contains(SourceRange other) {
        return start.compareTo(other.start) <= 0 && end.compareTo(other.end) >= 0;
    }
}
