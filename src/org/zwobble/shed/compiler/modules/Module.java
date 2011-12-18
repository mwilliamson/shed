package org.zwobble.shed.compiler.modules;

import lombok.Data;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;

@Data(staticConstructor="create")
public class Module {
    private final FullyQualifiedName identifier;
    private final Declaration declaration;
}
