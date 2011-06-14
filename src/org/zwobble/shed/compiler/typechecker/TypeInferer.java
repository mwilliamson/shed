package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.Result;
import org.zwobble.shed.compiler.parsing.SourcePosition;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.parsing.Result.fatal;
import static org.zwobble.shed.compiler.parsing.Result.success;

public class TypeInferer {
    public static Result<Type> inferType(ExpressionNode expression, StaticContext context) {
        if (expression instanceof BooleanLiteralNode) {
            return success(CoreTypes.BOOLEAN);            
        }
        if (expression instanceof NumberLiteralNode) {
            return success(CoreTypes.NUMBER);
        }
        if (expression instanceof StringLiteralNode) {
            return success(CoreTypes.STRING);
        }
        if (expression instanceof VariableIdentifierNode) {
            String identifier = ((VariableIdentifierNode)expression).getIdentifier();
            Option<Type> type = context.get(identifier);
            if (type.hasValue()) {
                return success(type.get());                
            } else {
                return fatal(asList(new CompilerError(
                    new SourcePosition(-1, -1),
                    new SourcePosition(-1, -1),
                    "No variable \"" + identifier + "\" in scope"
                )));
            }
        }
        if (expression instanceof ShortLambdaExpressionNode) {
            Result<Type> expressionTypeResult = inferType(((ShortLambdaExpressionNode)expression).getBody(), context);
            if (expressionTypeResult.anyErrors()) {
                return expressionTypeResult.changeValue(null);
            }
            return success((Type)new TypeApplication(CoreTypes.functionType(), asList(expressionTypeResult.get())));
        }
        throw new RuntimeException("Cannot infer type of expression: " + expression);
    }
}
