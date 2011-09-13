package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.parsing.SourceRange;

import lombok.Data;

@Data
public class SimpleCompilerError implements CompilerError {
    private final SourceRange location;
    private final String description;
}
