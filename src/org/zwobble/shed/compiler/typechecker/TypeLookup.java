package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.Type;

public interface TypeLookup {
    TypeResultWithValue<Type> lookupTypeReference(ExpressionNode typeReference);
}
