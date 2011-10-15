package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.ConditionTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class WhileStatementTypeChecker implements StatementTypeChecker<WhileStatementNode> {
    private final ConditionTypeChecker conditionTypeChecker;
    private final BlockTypeChecker blockTypeChecker;

    @Inject
    public WhileStatementTypeChecker(ConditionTypeChecker conditionTypeChecker, BlockTypeChecker blockTypeChecker) {
        this.conditionTypeChecker = conditionTypeChecker;
        this.blockTypeChecker = blockTypeChecker;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        WhileStatementNode statement, StaticContext context, Option<Type> returnType
    ) {
        TypeResult<?> conditionResult = conditionTypeChecker.typeAndCheckCondition(statement.getCondition(), context);
        TypeResult<?> bodyResult = blockTypeChecker.typeCheckBlock(statement.getBody(), context, returnType);
        return success(StatementTypeCheckResult.noReturn())
            .withErrorsFrom(conditionResult, bodyResult);
    }

}
