package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class FormalArgumentNode {
    private final String name;
    private final TypeReferenceNode type;
}
