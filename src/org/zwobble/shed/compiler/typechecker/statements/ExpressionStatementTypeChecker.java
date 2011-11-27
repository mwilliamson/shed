package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResults;
import org.zwobble.shed.compiler.types.Type;

public class ExpressionStatementTypeChecker implements StatementTypeChecker<ExpressionStatementNode> {
    private final TypeInferer typeInferer;

    @Inject
    public ExpressionStatementTypeChecker(TypeInferer typeInferer) {
        this.typeInferer = typeInferer;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ExpressionStatementNode statement, Option<Type> returnType) {
        TypeResult<Type> result = typeInferer.inferType(((ExpressionStatementNode) statement).getExpression());
        return TypeResults.success(StatementTypeCheckResult.noReturn()).withErrorsFrom(result);
    }
    
}
