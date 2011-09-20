package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.types.Type;

import lombok.Data;

@Data
public class ValueInfo {
    public static ValueInfo unassignableValue(Type type) {
        return new ValueInfo(type, false);
    }

    public static ValueInfo assignableValue(Type type) {
        return new ValueInfo(type, true);
    }
    
    private final Type type;
    private final boolean isAssignable;
}
