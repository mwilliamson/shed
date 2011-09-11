package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.zwobble.shed.compiler.Function0;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementTypeCheckResult;

import static org.zwobble.shed.compiler.typechecker.TypeChecker.typeCheckStatement;


public class BlockTypeChecker {
    public TypeResult<StatementTypeCheckResult> typeCheckBlock(
        List<StatementNode> statements,
        StaticContext context,
        NodeLocations nodeLocations
    ) {
        TypeResult<Void> result = TypeResult.success(null);
        
        for (StatementNode statement : statements) {
            if (statement instanceof DeclarationNode) {
                String identifier = ((DeclarationNode) statement).getIdentifier();
                context.declaredSoon(identifier);
            }
        }
        
        boolean hasReturnedYet = false;
        for (StatementNode statement : statements) {
            TypeResult<StatementTypeCheckResult> statementResult = typeCheckStatement(statement, nodeLocations, context);
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
