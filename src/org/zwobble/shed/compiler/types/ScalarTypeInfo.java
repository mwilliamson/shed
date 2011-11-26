package org.zwobble.shed.compiler.types;

import lombok.Data;

import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

@Data
public class ScalarTypeInfo {
    public static final ScalarTypeInfo EMPTY = new ScalarTypeInfo(interfaces(), members());
    
    private final Interfaces interfaces;
    private final Members members;
}
