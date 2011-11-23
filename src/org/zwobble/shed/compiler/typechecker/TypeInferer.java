package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionWithBodyNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

public interface TypeInferer {
    TypeResult<Type> inferType(ExpressionNode condition);
    TypeResult<ValueInfo> inferValueInfo(ExpressionNode expression);
    TypeResult<ValueInfo> inferFunctionType(FunctionNode function);
    Function<ValueInfo, TypeResult<ValueInfo>> typeCheckBody(FunctionWithBodyNode functionDeclaration);
}
