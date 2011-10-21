package org.zwobble.shed.compiler;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

@Data
public class CompilerErrorWithSyntaxNode implements CompilerError {
    private final SyntaxNode node;
    private final CompilerErrorDescription description;
    
    public String describe() {
        return description.describe();
    }
}
