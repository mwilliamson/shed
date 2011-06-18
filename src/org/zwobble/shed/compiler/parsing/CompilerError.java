package org.zwobble.shed.compiler.parsing;

import lombok.Data;

@Data
public class CompilerError {
    private final SourceRange location;
    private final String description;
}
