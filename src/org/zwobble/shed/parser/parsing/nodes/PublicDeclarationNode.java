package org.zwobble.shed.parser.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class PublicDeclarationNode {
    private final List<String> identifiers;
}
