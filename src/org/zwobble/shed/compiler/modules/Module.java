package org.zwobble.shed.compiler.modules;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;

public interface Module {
    Declaration getDeclaration();
    FullyQualifiedName getName();
}
