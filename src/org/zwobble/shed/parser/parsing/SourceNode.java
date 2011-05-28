package org.zwobble.shed.parser.parsing;

import java.util.List;

import lombok.Data;

@Data
public class SourceNode {
    private final PackageDeclarationNode packageDeclaration;
    private final List<ImportNode> imports;
}
