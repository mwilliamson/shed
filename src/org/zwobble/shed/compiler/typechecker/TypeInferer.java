package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

public class TypeInferer {
    public static Type inferType(ExpressionNode expression, StaticContext context) {
        if (expression instanceof BooleanLiteralNode) {
            return CoreTypes.BOOLEAN;            
        }
        if (expression instanceof NumberLiteralNode) {
            return CoreTypes.NUMBER;
        }
        if (expression instanceof StringLiteralNode) {
            return CoreTypes.STRING;
        }
        if (expression instanceof VariableIdentifierNode) {
            return context.get(((VariableIdentifierNode)expression).getIdentifier());
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }
}
