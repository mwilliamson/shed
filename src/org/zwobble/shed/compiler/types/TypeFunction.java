package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

@Data
public class TypeFunction implements Type {
    private final Type baseType;
    private final List<FormalTypeParameter> typeParameters;
    
    public String shortName() {
        return baseType.shortName();
    }
}
