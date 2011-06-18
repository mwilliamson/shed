package org.zwobble.shed.compiler.parsing;

import lombok.Data;

@Data
public class SourceRange {
    private final SourcePosition start;
    private final SourcePosition end;
}
