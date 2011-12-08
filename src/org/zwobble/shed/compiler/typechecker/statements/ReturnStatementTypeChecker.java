package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.typechecker.SubTyping;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultBuilder;
import org.zwobble.shed.compiler.typechecker.errors.CannotReturnHereError;
import org.zwobble.shed.compiler.typechecker.errors.WrongReturnTypeError;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.errors.CompilerErrors.error;

import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;

public class ReturnStatementTypeChecker implements StatementTypeChecker<ReturnNode> {
    private final TypeInferer typeInferer;
    private final SubTyping subTyping;

    @Inject
    public ReturnStatementTypeChecker(TypeInferer typeInferer, SubTyping subTyping) {
        this.typeInferer = typeInferer;
        this.subTyping = subTyping;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ReturnNode returnStatement, Option<Type> returnType) {
        TypeResultBuilder<StatementTypeCheckResult> typeResult = typeResultBuilder(StatementTypeCheckResult.alwaysReturns());
        ExpressionNode expression = returnStatement.getExpression();
        TypeResult<Type> expressionTypeResult = typeInferer.inferType(expression);
        typeResult.addErrors(expressionTypeResult);
        if (!returnType.hasValue()) {
            typeResult.addError(error(returnStatement, new CannotReturnHereError()));
        } else if (expressionTypeResult.hasValue() && !subTyping.isSubType(expressionTypeResult.getOrThrow(), returnType.get())) {
            typeResult.addError(error(expression, new WrongReturnTypeError(returnType.get(), expressionTypeResult.getOrThrow())));
        }
        return typeResult.build();
    }
}
