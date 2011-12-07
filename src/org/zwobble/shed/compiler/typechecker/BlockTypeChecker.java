package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.statements.AllStatementsTypeChecker;
import org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;


public class BlockTypeChecker {
    private final AllStatementsTypeChecker statementsTypeChecker;

    @Inject
    public BlockTypeChecker(AllStatementsTypeChecker statementsTypeChecker) {
        this.statementsTypeChecker = statementsTypeChecker;
    }
    
    public TypeResultWithValue<StatementTypeCheckResult> forwardDeclareAndTypeCheck(
        Iterable<StatementNode> statements,
        Option<Type> returnType
    ) {
        TypeResult<?> result = forwardDeclare(statements);
        return typeCheck(statements, returnType).withErrorsFrom(result);
    }

    public TypeResult<?> forwardDeclare(Iterable<? extends StatementNode> statements) {
        TypeResultBuilder<Void> result = typeResultBuilder();
        for (StatementNode statement : statements) {
            TypeResult<?> statementResult = statementsTypeChecker.forwardDeclare(statement);
            result.addErrors(statementResult);
        }
        return result.build();
    }

    public TypeResultWithValue<StatementTypeCheckResult> typeCheck(Iterable<StatementNode> statements, Option<Type> returnType) {
        TypeResultBuilder<Void> result = typeResultBuilder();
        boolean hasReturnedYet = false;
        for (StatementNode statement : statements) {
            TypeResult<StatementTypeCheckResult> statementResult = statementsTypeChecker.typeCheck(statement, returnType);
            result.addErrors(statementResult);
            if (statementResult.hasValue()) {
                hasReturnedYet |= statementResult.getOrThrow().hasReturned();   
            }
        }
        final boolean hasReturned = hasReturnedYet;
        return result.buildWithValue(new StatementTypeCheckResult(hasReturned));
    }
}
