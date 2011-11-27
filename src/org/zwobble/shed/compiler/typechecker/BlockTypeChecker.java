package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.statements.AllStatementsTypeChecker;
import org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;


public class BlockTypeChecker {
    private final AllStatementsTypeChecker statementsTypeChecker;

    @Inject
    public BlockTypeChecker(AllStatementsTypeChecker statementsTypeChecker) {
        this.statementsTypeChecker = statementsTypeChecker;
    }
    
    public TypeResult<StatementTypeCheckResult> forwardDeclareAndTypeCheck(
        Iterable<StatementNode> statements,
        Option<Type> returnType
    ) {
        TypeResult<?> result = forwardDeclare(statements);
        return typeCheck(statements, returnType).withErrorsFrom(result);
    }

    public TypeResult<?> forwardDeclare(Iterable<? extends StatementNode> statements) {
        TypeResult<Void> result = TypeResult.success();
        for (StatementNode statement : statements) {
            TypeResult<?> statementResult = statementsTypeChecker.forwardDeclare(statement);
            result = result.withErrorsFrom(statementResult);
        }
        return result;
    }

    public TypeResult<StatementTypeCheckResult> typeCheck(Iterable<StatementNode> statements, Option<Type> returnType) {
        TypeResult<Void> result = success();
        boolean hasReturnedYet = false;
        for (StatementNode statement : statements) {
            TypeResult<StatementTypeCheckResult> statementResult = statementsTypeChecker.typeCheck(statement, returnType);
            result = result.withErrorsFrom(statementResult);
            if (statementResult.hasValue()) {
                hasReturnedYet |= statementResult.get().hasReturned();   
            }
        }
        final boolean hasReturned = hasReturnedYet;
        return success(new StatementTypeCheckResult(hasReturned)).withErrorsFrom(result);
    }
}
