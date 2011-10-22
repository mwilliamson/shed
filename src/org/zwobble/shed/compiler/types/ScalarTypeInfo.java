package org.zwobble.shed.compiler.types;

import java.util.Map;
import java.util.Set;

import lombok.Data;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import static org.zwobble.shed.compiler.types.Members.members;

import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

@Data
public class ScalarTypeInfo {
    public static final ScalarTypeInfo EMPTY = new ScalarTypeInfo(interfaces(), members());
    
    private final Set<Type> superTypes;
    private final Map<String, ValueInfo> members;
}
