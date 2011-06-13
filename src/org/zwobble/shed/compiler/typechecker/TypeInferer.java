package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

public class TypeInferer {
    public static Type inferType(ExpressionNode expression) {
        return CoreTypes.BOOLEAN;
    }
}
