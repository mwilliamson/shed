package org.zwobble.shed.compiler;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;

@Data
public class SourceFileCompilationResult {
    private final Iterable<TokenPosition> tokens;
    private final NodeLocations nodeLocations;
    private final List<CompilerError> errors;
    private final String javaScript;
    
    public boolean isSuccess() {
        return javaScript != null;
    }
}
