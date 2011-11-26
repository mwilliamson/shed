package org.zwobble.shed.compiler.types;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import lombok.Data;

@Data(staticConstructor="member")
public class Member {
    private final String name;
    private final ValueInfo valueInfo;
    
    public Type getType() {
        return valueInfo.getType();
    }
}
