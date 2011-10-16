package org.zwobble.shed.compiler.types;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;

public interface ScalarType extends Type {
    FullyQualifiedName getFullyQualifiedName();
}
