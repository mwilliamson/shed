package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.ConditionTypeChecker;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResults;
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
    public TypeResult<StatementTypeCheckResult> typeCheck(IfThenElseStatementNode statement, Option<Type> returnType) {
        TypeResult<Void> conditionResult = conditionTypeChecker.typeAndCheckCondition(statement.getCondition());
        
        TypeResult<StatementTypeCheckResult> ifTrueResult = typeCheckBlock(statement.getIfTrue(), returnType);
        TypeResult<StatementTypeCheckResult> ifFalseResult = typeCheckBlock(statement.getIfFalse(), returnType);
        
        boolean returns = 
            ifTrueResult.hasValue() && ifTrueResult.getOrThrow().hasReturned() && 
            ifFalseResult.hasValue() && ifFalseResult.getOrThrow().hasReturned();
        
        return TypeResults.success(StatementTypeCheckResult.doesReturn(returns))
            .withErrorsFrom(conditionResult)
            .withErrorsFrom(ifTrueResult)
            .withErrorsFrom(ifFalseResult);
    }

    private TypeResult<StatementTypeCheckResult> typeCheckBlock(BlockNode statements, Option<Type> returnType) {
        return blockTypeChecker.forwardDeclareAndTypeCheck(statements, returnType);
    }
}
