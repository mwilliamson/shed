package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class TypeIdentifierNode implements TypeReferenceNode {
    public final String identifier;
}
