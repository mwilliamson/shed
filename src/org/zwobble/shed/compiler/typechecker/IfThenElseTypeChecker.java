package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.typechecker.statements.StatementTypeChecker;
import org.zwobble.shed.compiler.types.Type;

public class IfThenElseTypeChecker implements StatementTypeChecker<IfThenElseStatementNode> {
    private final ConditionTypeChecker conditionTypeChecker;
    private final BlockTypeChecker blockTypeChecker;

    @Inject
    public IfThenElseTypeChecker(ConditionTypeChecker conditionTypeChecker, BlockTypeChecker blockTypeChecker) {
        this.conditionTypeChecker = conditionTypeChecker;
        this.blockTypeChecker = blockTypeChecker;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        IfThenElseStatementNode statement, StaticContext context, Option<Type> returnType
    ) {
        TypeResult<Void> conditionResult = conditionTypeChecker.typeAndCheckCondition(statement.getCondition(), context);
        
        TypeResult<StatementTypeCheckResult> ifTrueResult = typeCheckBlock(statement.getIfTrue(), context, returnType);
        TypeResult<StatementTypeCheckResult> ifFalseResult = typeCheckBlock(statement.getIfFalse(), context, returnType);
        
        boolean returns = 
            ifTrueResult.hasValue() && ifTrueResult.get().hasReturned() && 
            ifFalseResult.hasValue() && ifFalseResult.get().hasReturned();
        
        return TypeResult.success(StatementTypeCheckResult.doesReturn(returns))
            .withErrorsFrom(conditionResult)
            .withErrorsFrom(ifTrueResult)
            .withErrorsFrom(ifFalseResult);
    }

    private TypeResult<StatementTypeCheckResult> typeCheckBlock(
        BlockNode statements, StaticContext context, Option<Type> returnType
    ) {
        return blockTypeChecker.typeCheckBlock(statements, context, returnType);
    }
}
