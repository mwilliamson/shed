package org.zwobble.shed.compiler.types;

import java.util.Set;

public interface ScalarType extends Type {
    Set<InterfaceType> superTypes();
}
