package org.zwobble.shed.compiler.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import lombok.Data;

@Data
public class ClassType implements ScalarType {
    private final List<String> scope;
    private final String name;
    private final Set<InterfaceType> superTypes;
    private final Map<String, ValueInfo> members;
    
    @Override
    public String shortName() {
        return name;
    }
    
    @Override
    public Set<InterfaceType> superTypes() {
        return superTypes;
    }
}
