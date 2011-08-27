package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class PublicDeclarationNode implements StatementNode {
    private final DeclarationNode declaration;
}
