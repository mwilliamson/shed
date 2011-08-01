package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class SourceNode implements SyntaxNode {
    private final PackageDeclarationNode packageDeclaration;
    private final List<ImportNode> imports;
    private final List<StatementNode> statements;
}
