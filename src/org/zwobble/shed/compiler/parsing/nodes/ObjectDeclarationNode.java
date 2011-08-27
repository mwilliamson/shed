package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class ObjectDeclarationNode implements StatementNode {
    private final String name;
    private final List<StatementNode> statements;
}
