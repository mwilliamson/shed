package org.zwobble.shed.compiler.types;

import java.util.Map;
import java.util.Set;

import lombok.Data;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

@Data
public class ScalarTypeInfo {
    private final Set<InterfaceType> superTypes;
    private final Map<String, ValueInfo> members;
}
