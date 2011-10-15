package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionWithBodyNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

public interface TypeInferer {
    TypeResult<Type> inferType(ExpressionNode condition, StaticContext context);
    TypeResult<ValueInfo> inferValueInfo(ExpressionNode expression, StaticContext context);
    TypeResult<ValueInfo> inferFunctionType(FunctionWithBodyNode functionDeclaration, StaticContext context);
    Function<ValueInfo, TypeResult<ValueInfo>> typeCheckBody(FunctionWithBodyNode functionDeclaration, StaticContext context);
}
