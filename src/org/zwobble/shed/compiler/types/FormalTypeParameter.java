package org.zwobble.shed.compiler.types;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class FormalTypeParameter implements Type {
    public static FormalTypeParameter invariantFormalTypeParameter(String name) {
        return new FormalTypeParameter(name);
    }
    
    private FormalTypeParameter(String name) {
        this.name = name;
    }
    
    private final String name;
    
    @Override
    public String shortName() {
        return name;
    }
}
