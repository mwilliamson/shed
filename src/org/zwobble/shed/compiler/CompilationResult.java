package org.zwobble.shed.compiler;

import java.util.List;

import org.zwobble.shed.compiler.errors.CompilerError;

public class CompilationResult {
    private final List<CompilerError> errors;
    private final String output;

    public CompilationResult(List<CompilerError> errors, String output) {
        this.errors = errors;
        this.output = output;
    }
    
    public boolean isSuccess() {
        return errors.isEmpty();
    }
    
    public List<CompilerError> errors() {
        return errors;
    }
    
    public String output() {
        return output;
    }
}
