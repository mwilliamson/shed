package org.zwobble.shed.parser.parsing.nodes;

import java.util.List;

import lombok.Data;

@Data
public class TypeApplicationNode implements TypeReferenceNode {
    private final TypeReferenceNode baseType;
    private final List<TypeReferenceNode> parameters;
}
