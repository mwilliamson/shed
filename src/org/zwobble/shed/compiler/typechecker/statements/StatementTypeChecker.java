package org.zwobble.shed.compiler.typechecker.statements;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.types.Type;

public interface StatementTypeChecker<T extends StatementNode> {
    TypeResult<StatementTypeCheckResult>typeCheck(T statement, Option<Type> returnType);
}
