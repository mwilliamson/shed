package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class GlobalDeclarationNode implements DeclarationNode {
    private final String identifier;
}
