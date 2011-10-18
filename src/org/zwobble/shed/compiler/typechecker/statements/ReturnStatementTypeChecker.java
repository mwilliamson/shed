package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.errors.CannotReturnHereError;
import org.zwobble.shed.compiler.typechecker.errors.WrongReturnTypeError;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class ReturnStatementTypeChecker implements StatementTypeChecker<ReturnNode> {
    private final TypeInferer typeInferer;
    private final NodeLocations nodeLocations;
    private final StaticContext context;

    @Inject
    public ReturnStatementTypeChecker(TypeInferer typeInferer, NodeLocations nodeLocations, StaticContext context) {
        this.typeInferer = typeInferer;
        this.nodeLocations = nodeLocations;
        this.context = context;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ReturnNode returnStatement, Option<Type> returnType) {
        if (!returnType.hasValue()) {
            return failure(
                StatementTypeCheckResult.alwaysReturns(),
                new CompilerError(
                    nodeLocations.locate(returnStatement),
                    new CannotReturnHereError()
                )
            );
        }
        ExpressionNode expression = returnStatement.getExpression();
        TypeResult<Type> expressionType = typeInferer.inferType(expression);
        if (!expressionType.isSuccess()) {
            return failure(expressionType.getErrors());
        }
        if (isSubType(expressionType.get(), returnType.get(), context)) {
            return success(StatementTypeCheckResult.alwaysReturns());
        } else {
            return failure(
                StatementTypeCheckResult.alwaysReturns(),
                new CompilerError(
                    nodeLocations.locate(expression),
                    new WrongReturnTypeError(returnType.get(), expressionType.get())
                )
            );
        }
    }
}
