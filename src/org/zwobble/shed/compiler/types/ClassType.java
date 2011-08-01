package org.zwobble.shed.compiler.types;

import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class ClassType implements ScalarType {
    private final List<String> scope;
    private final String name;
    private final Set<InterfaceType> superTypes;
    
    @Override
    public String shortName() {
        return name;
    }
    
    @Override
    public Set<InterfaceType> superTypes() {
        return superTypes;
    }
}
