package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class NumberLiteralNode implements LiteralNode {
    private final String value;
}
