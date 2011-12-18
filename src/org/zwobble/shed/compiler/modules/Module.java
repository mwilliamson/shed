package org.zwobble.shed.compiler.modules;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;

import lombok.Data;

@Data(staticConstructor="create")
public class Module {
    private final FullyQualifiedName identifier;
}
