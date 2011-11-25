package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.types.Type;

public interface ArgumentTypeInferer {
    TypeResult<List<Type>> inferArgumentTypesAndAddToContext(List<FormalArgumentNode> formalArguments);
}
