package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class ObjectDeclarationNode implements DeclarationNode {
    private final String identifier;
    private final List<StatementNode> statements;
}
