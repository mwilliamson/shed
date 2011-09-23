package org.zwobble.shed.compiler.typechecker;

import lombok.Data;

import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.types.Type;

@Data
public class FormalArgumentType {
    private final Type type;
    private final FormalArgumentNode node;
}
