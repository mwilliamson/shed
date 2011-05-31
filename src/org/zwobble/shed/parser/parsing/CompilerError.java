package org.zwobble.shed.parser.parsing;

import lombok.Data;

@Data
public class CompilerError {
    private final int lineNumber;
    private final int characterNumber;
    private final String description;
}
