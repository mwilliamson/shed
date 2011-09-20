package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.types.Type;

import lombok.Data;

@Data
public class ValueInfo {
    public static ValueInfo valueInfo(Type type) {
        return new ValueInfo(type);
    }
    
    private final Type type;
}
