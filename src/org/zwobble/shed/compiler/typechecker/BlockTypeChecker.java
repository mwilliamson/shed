package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Function0;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.statements.AllStatementsTypeChecker;
import org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult;
import org.zwobble.shed.compiler.types.Type;


public class BlockTypeChecker {
    private final AllStatementsTypeChecker statementsTypeChecker;

    @Inject
    public BlockTypeChecker(AllStatementsTypeChecker statementsTypeChecker) {
        this.statementsTypeChecker = statementsTypeChecker;
    }
    
    public TypeResult<StatementTypeCheckResult> forwardDeclareAndTypeCheck(
        Iterable<StatementNode> statements,
        StaticContext context,
        Option<Type> returnType
    ) {
        TypeResult<?> result = forwardDeclare(statements, context);
        return typeCheck(statements, context, returnType).withErrorsFrom(result);
    }

    public TypeResult<?> forwardDeclare(Iterable<StatementNode> statements, StaticContext context) {
        TypeResult<Void> result = TypeResult.success();
        for (StatementNode statement : statements) {
            TypeResult<?> statementResult = statementsTypeChecker.forwardDeclare(statement, context);
            result = result.withErrorsFrom(statementResult);
        }
        return result;
    }

    public TypeResult<StatementTypeCheckResult> typeCheck(Iterable<StatementNode> statements, StaticContext context, Option<Type> returnType) {
        TypeResult<Void> result = TypeResult.success();
        boolean hasReturnedYet = false;
        for (StatementNode statement : statements) {
            TypeResult<StatementTypeCheckResult> statementResult = statementsTypeChecker.typeCheck(statement, context, returnType);
            result = result.withErrorsFrom(statementResult);
            if (statementResult.hasValue()) {
                hasReturnedYet |= statementResult.get().hasReturned();   
            }
        }
        final boolean hasReturned = hasReturnedYet;
        return result.then(new Function0<TypeResult<StatementTypeCheckResult>>() {
            @Override
            public TypeResult<StatementTypeCheckResult> apply() {
                return TypeResult.success(new StatementTypeCheckResult(hasReturned));
            }
        });
    }
}
