package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class ObjectDeclarationNode implements DeclarationNode {
    private final String identifier;
    private final BlockNode statements;
}
