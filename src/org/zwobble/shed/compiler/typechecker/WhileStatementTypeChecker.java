package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class WhileStatementTypeChecker implements StatementTypeChecker<WhileStatementNode> {
    private final ConditionTypeChecker conditionTypeChecker;
    private final BlockTypeChecker blockTypeChecker;

    public WhileStatementTypeChecker(ConditionTypeChecker conditionTypeChecker, BlockTypeChecker blockTypeChecker) {
        this.conditionTypeChecker = conditionTypeChecker;
        this.blockTypeChecker = blockTypeChecker;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        WhileStatementNode statement, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {
        TypeResult<?> conditionResult = conditionTypeChecker.typeAndCheckCondition(statement.getCondition(), nodeLocations, context);
        TypeResult<?> bodyResult = blockTypeChecker.typeCheckBlock(statement.getBody(), nodeLocations, context, returnType);
        return success(StatementTypeCheckResult.noReturn())
            .withErrorsFrom(conditionResult, bodyResult);
    }

}
