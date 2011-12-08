package org.zwobble.shed.compiler.errors;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SimpleErrorDescription implements CompilerErrorDescription {
    private final String description;
    
    @Override
    public String describe() {
        return description;
    }
}
