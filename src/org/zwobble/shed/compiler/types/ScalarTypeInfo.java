package org.zwobble.shed.compiler.types;

import java.util.Set;

import lombok.Data;

import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

@Data
public class ScalarTypeInfo {
    public static final ScalarTypeInfo EMPTY = new ScalarTypeInfo(interfaces(), members());
    
    private final Set<Type> superTypes;
    private final Members members;
}
