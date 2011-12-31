package org.zwobble.shed.compiler.modules;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;

import lombok.Data;

@Data(staticConstructor="create")
public class SimpleModule implements Module {
    private final FullyQualifiedName name;
    private final Declaration declaration;
}
