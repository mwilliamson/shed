package org.zwobble.shed.compiler.types;

import java.util.Map;
import java.util.Set;

public interface ScalarType extends Type {
    Set<InterfaceType> superTypes();
    Map<String, Type> getMembers();
}
