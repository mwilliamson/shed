package org.zwobble.shed.compiler.typechecker.expressions;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.typechecker.FunctionTypeChecker;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;

public class LongLambdaExpressionTypeInferer implements ExpressionTypeInferer<LongLambdaExpressionNode> {
    private final FunctionTypeChecker functionTypeChecker;

    @Inject
    public LongLambdaExpressionTypeInferer(FunctionTypeChecker functionTypeChecker) {
        this.functionTypeChecker = functionTypeChecker;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(LongLambdaExpressionNode function) {
        TypeResult<ValueInfo> typeResult = functionTypeChecker.inferFunctionType(function);
        return typeResult.ifValueThen(functionTypeChecker.typeCheckBody(function));
    }

}
