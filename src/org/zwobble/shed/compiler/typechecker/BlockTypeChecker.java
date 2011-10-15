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
    
    public TypeResult<StatementTypeCheckResult> typeCheckBlock(
        Iterable<StatementNode> statements,
        StaticContext context,
        Option<Type> returnType
    ) {
        TypeResult<Void> result = TypeResult.success(null);
        
        for (StatementNode statement : statements) {
            TypeResult<?> statementResult = statementsTypeChecker.forwardDeclare(statement, context);
            result = result.withErrorsFrom(statementResult);
        }
        
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
