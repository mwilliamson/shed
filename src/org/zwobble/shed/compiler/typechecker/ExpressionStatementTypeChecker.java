package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.types.Type;

public class ExpressionStatementTypeChecker implements StatementTypeChecker<ExpressionStatementNode> {
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        ExpressionStatementNode statement, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {
        TypeResult<Type> result = TypeInferer.inferType(((ExpressionStatementNode) statement).getExpression(), nodeLocations, context);
        return TypeResult.success(StatementTypeCheckResult.noReturn()).withErrorsFrom(result);
    }
    
}
