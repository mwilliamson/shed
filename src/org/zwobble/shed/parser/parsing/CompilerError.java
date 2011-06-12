package org.zwobble.shed.parser.parsing;

import lombok.Data;

@Data
public class CompilerError {
    private final SourcePosition start;
    private final SourcePosition end;
    private final String description;
}
