package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class FormalArgumentNode {
    private final String name;
    private final TypeReferenceNode type;
}
