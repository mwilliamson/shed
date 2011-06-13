package org.zwobble.shed.compiler.parsing;

import lombok.Data;

@Data
public class SourcePosition {
    private final int lineNumber;
    private final int characterNumber;
}
