package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class GlobalDeclarationNode implements Declaration {
    private final String identifier;
}
