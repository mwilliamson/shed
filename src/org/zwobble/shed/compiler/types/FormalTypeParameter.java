package org.zwobble.shed.compiler.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class FormalTypeParameter implements Type {
    private final String name;
    
    @Override
    public String shortName() {
        return name;
    }
}
