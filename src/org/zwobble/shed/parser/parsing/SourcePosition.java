package org.zwobble.shed.parser.parsing;

import lombok.Data;

@Data
public class SourcePosition {
    private final int lineNumber;
    private final int characterNumber;
}
