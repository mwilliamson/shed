package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class ObjectDeclarationNode implements TypeDeclarationNode {
    private final String identifier;
    private final BlockNode statements;
}
