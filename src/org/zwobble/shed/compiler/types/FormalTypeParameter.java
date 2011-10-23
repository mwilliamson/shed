package org.zwobble.shed.compiler.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class FormalTypeParameter implements Type {
    public static FormalTypeParameter formalTypeParameter(String name) {
        return new FormalTypeParameter(name);
    }
    
    private final String name;
    
    @Override
    public String shortName() {
        return name;
    }
}
