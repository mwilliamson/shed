package org.zwobble.shed.parser.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class SourceNode {
    private final PackageDeclarationNode packageDeclaration;
    private final List<ImportNode> imports;
    private final PublicDeclarationNode publicDeclaration;
    private final List<StatementNode> statements;
}
