package org.zwobble.shed.compiler;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.tokeniser.Tokens;

@Data
public class CompilationResult {
    private final Tokens tokens;
    private final List<CompilerError> errors;
    private final String javaScript;
    
    public boolean isSuccess() {
        return javaScript != null;
    }
}
