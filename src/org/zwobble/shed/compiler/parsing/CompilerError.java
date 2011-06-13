package org.zwobble.shed.compiler.parsing;

import lombok.Data;

@Data
public class CompilerError {
    private final SourcePosition start;
    private final SourcePosition end;
    private final String description;
}
