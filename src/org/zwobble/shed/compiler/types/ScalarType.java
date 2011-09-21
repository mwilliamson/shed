package org.zwobble.shed.compiler.types;

import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.typechecker.ValueInfo;

public interface ScalarType extends Type {
    FullyQualifiedName getFullyQualifiedName();
    Set<InterfaceType> superTypes();
    Map<String, ValueInfo> getMembers();
}
