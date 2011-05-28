package org.zwobble.shed.parser.parsing;

import java.util.List;

import lombok.Data;

@Data
public class PackageDeclarationNode {
    private final List<String> packageNames;
}
