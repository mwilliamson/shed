package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;

import lombok.Data;

@Data(staticConstructor="build")
public class SyntaxNodeStructure {
    public static final SyntaxNodeStructure LEAF = build(Collections.<SyntaxNode>emptyList());
    
    private final Iterable<? extends SyntaxNode> children;
}
