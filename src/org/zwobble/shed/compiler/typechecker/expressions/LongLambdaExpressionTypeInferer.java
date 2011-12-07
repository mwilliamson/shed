package org.zwobble.shed.compiler.typechecker.expressions;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;

public class LongLambdaExpressionTypeInferer implements ExpressionTypeInferer<LongLambdaExpressionNode> {
    private final TypeInferer typeInferer;

    @Inject
    public LongLambdaExpressionTypeInferer(TypeInferer typeInferer) {
        this.typeInferer = typeInferer;
    }
    
    @Override
    public TypeResult<ValueInfo> inferValueInfo(LongLambdaExpressionNode function) {
        TypeResult<ValueInfo> typeResult = typeInferer.inferFunctionType(function);
        return typeResult.ifValueThen(typeInferer.typeCheckBody(function));
    }

}
