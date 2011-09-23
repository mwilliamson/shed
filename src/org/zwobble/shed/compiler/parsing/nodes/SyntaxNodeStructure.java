package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data(staticConstructor="build")
public class SyntaxNodeStructure {
    private final Iterable<? extends SyntaxNode> children;
}
