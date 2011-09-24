package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.Type;

public interface StatementTypeChecker<T extends StatementNode> {
    TypeResult<StatementTypeCheckResult>typeCheck(
        T statement, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    );
}
