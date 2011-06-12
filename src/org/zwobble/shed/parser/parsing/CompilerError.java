package org.zwobble.shed.parser.parsing;

import lombok.Data;

@Data
public class CompilerError {
    private final SourcePosition from;
    private final SourcePosition to;
    private final String description;
}
