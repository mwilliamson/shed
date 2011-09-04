package org.zwobble.shed.compiler;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;

@Data
public class CompilationResult {
    private final List<TokenPosition> tokens;
    private final List<CompilerError> errors;
    private final String javaScript;
}
