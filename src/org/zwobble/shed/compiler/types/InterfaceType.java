package org.zwobble.shed.compiler.types;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class InterfaceType implements ScalarType {
    private final List<String> scope;
    private final String name;
    
    @Override
    public String shortName() {
        return name;
    }
    
    @Override
    public Set<InterfaceType> superTypes() {
        return Collections.emptySet();
    }
}
