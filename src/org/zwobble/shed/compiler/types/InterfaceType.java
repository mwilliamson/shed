package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

@Data
public class InterfaceType implements ScalarType {
    private final List<String> scope;
    private final String name;
    
    @Override
    public String shortName() {
        return name;
    }

}
