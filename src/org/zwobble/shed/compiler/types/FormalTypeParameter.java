package org.zwobble.shed.compiler.types;

import lombok.Data;

@Data
public class FormalTypeParameter implements Type {
    private final String name;
    
    @Override
    public String shortName() {
        return name;
    }
}
