package org.zwobble.shed.parser.parsing.nodes;

import lombok.Data;

@Data
public class TypeIdentifierNode implements TypeReferenceNode {
    public final String identifier;
}
