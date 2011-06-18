package org.zwobble.shed.compiler.parsing;

import lombok.Data;

@Data
public class SourcePosition implements Comparable<SourcePosition> {
    private final int lineNumber;
    private final int characterNumber;
    
    @Override
    public int compareTo(SourcePosition other) {
        int lineNumberDifference = lineNumber - other.lineNumber;
        if (lineNumberDifference != 0) {
            return lineNumberDifference;
        }
        return characterNumber - other.characterNumber;
    }
}
