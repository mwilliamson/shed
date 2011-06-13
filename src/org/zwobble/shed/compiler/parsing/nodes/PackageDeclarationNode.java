package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class PackageDeclarationNode {
    private final List<String> packageNames;
}
