package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Function0;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeChecker.typeCheckStatement;


public class BlockTypeChecker {
    public TypeResult<StatementTypeCheckResult> typeCheckBlock(
        Iterable<StatementNode> statements,
        StaticContext context,
        NodeLocations nodeLocations,
        Option<Type> returnType
    ) {
        TypeResult<Void> result = TypeResult.success(null);
        
        boolean hasReturnedYet = false;
        for (StatementNode statement : statements) {
            TypeResult<StatementTypeCheckResult> statementResult = typeCheckStatement(statement, nodeLocations, context, returnType);
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
