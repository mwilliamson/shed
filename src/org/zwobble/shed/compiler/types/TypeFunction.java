package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

@Data
public class TypeFunction implements Type {
    // TODO: instead of having a scope and name, wrap a ScalarType so that
    // this is easily extended to, say, an InterfaceType. ScalarType/InterfaceType can then
    // refer to a formal parameter as a type, and TypeFunction/TypeApplication are responsible for converting them to concrete types
    
    private final Type baseType;
    private final List<FormalTypeParameter> typeParameters;
    
    @Override
    public String shortName() {
        return baseType.shortName();
    }
}
