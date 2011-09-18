package org.zwobble.shed.compiler;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.SourceRange;

@Data
public class CompilerError {
    public static CompilerError error(SourceRange location, String description) {
        return new CompilerError(location, new SimpleErrorDescription(description));
    }
    
    private final SourceRange location;
    private final CompilerErrorDescription description;
    
    public String describe() {
        return description.describe();
    }
}
