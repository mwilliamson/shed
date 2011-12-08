package org.zwobble.shed.compiler.errors;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.SourceRange;

@Data
public class CompilerErrorWithLocation implements CompilerError {
    private final SourceRange location;
    private final CompilerErrorDescription description;
    
    public String describe() {
        return description.describe();
    }
}
