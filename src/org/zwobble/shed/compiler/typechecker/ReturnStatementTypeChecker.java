package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;
import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class ReturnStatementTypeChecker {
    public static TypeResult<StatementTypeCheckResult> typeCheckReturnStatement(ReturnNode returnStatement, NodeLocations nodeLocations, StaticContext context) {
        Option<Type> expectedReturnType = context.currentScope().getReturnType();
        if (!expectedReturnType.hasValue()) {
            return failure(StatementTypeCheckResult.alwaysReturns(), asList(
                CompilerError.error(
                    nodeLocations.locate(returnStatement),
                    "Cannot return from this scope"
                )
            ));
        }
        ExpressionNode expression = returnStatement.getExpression();
        TypeResult<Type> expressionType = inferType(expression, nodeLocations, context);
        if (!expressionType.isSuccess()) {
            return failure(expressionType.getErrors());
        }
        if (isSubType(expressionType.get(), expectedReturnType.get())) {
            return success(StatementTypeCheckResult.alwaysReturns());
        } else {
            String expectedName = expectedReturnType.get().shortName();
            String actualName = expressionType.get().shortName();
            return failure(StatementTypeCheckResult.alwaysReturns(), asList(
                CompilerError.error(
                    nodeLocations.locate(expression),
                    "Expected return expression of type \"" + expectedName + "\" but was of type \"" + actualName + "\""
                )
            ));
        }
    }
}
