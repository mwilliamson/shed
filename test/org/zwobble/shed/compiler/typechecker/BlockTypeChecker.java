package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.Function0;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

import static org.zwobble.shed.compiler.typechecker.TypeChecker.typeCheckStatement;


public class BlockTypeChecker {
    public TypeResult<Result> typeCheckBlock(
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
            TypeResult<Void> statementResult = typeCheckStatement(statement, nodeLocations, context);
            result = result.withErrorsFrom(statementResult);
            if (statement instanceof ReturnNode) {
                hasReturnedYet = true;
            }
        }
        final boolean hasReturned = hasReturnedYet;
        return result.then(new Function0<TypeResult<Result>>() {
            @Override
            public TypeResult<Result> apply() {
                return TypeResult.success(new Result(hasReturned));
            }
        });
    }
    
    @Data
    public class Result {
        private final boolean hasReturned;
    }
}